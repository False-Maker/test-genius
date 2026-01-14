# Oracle 数据库备份完整操作流程

## 目录
1. [备份前准备](#1-备份前准备)
2. [RMAN 在线备份完整流程](#2-rman-在线备份完整流程)
3. [备份期间数据处理机制](#3-备份期间数据处理机制)
4. [Data Pump 备份流程](#4-data-pump-备份流程)
5. [备份验证与恢复](#5-备份验证与恢复)
6. [自动化备份方案](#6-自动化备份方案)
7. [常见问题处理](#7-常见问题处理)

---

## 1. 备份前准备

### 1.1 检查数据库状态

```bash
# 连接到数据库
sqlplus / as sysdba

# 检查数据库状态
SELECT status FROM v$instance;
-- 应该显示：OPEN

# 检查数据库版本
SELECT * FROM v$version;

# 检查数据库名称
SELECT name FROM v$database;
```

### 1.2 检查归档模式（关键！）

```sql
-- 检查归档模式
SELECT log_mode FROM v$database;

-- 如果显示 ARCHIVELOG，可以继续
-- 如果显示 NOARCHIVELOG，需要切换到归档模式
```

#### 切换到归档模式（仅需一次，需要短暂停机）

```sql
-- 步骤1：关闭数据库
SHUTDOWN IMMEDIATE;

-- 步骤2：启动到mount状态
STARTUP MOUNT;

-- 步骤3：启用归档模式
ALTER DATABASE ARCHIVELOG;

-- 步骤4：打开数据库
ALTER DATABASE OPEN;

-- 步骤5：验证
SELECT log_mode FROM v$database;
-- 应该显示：ARCHIVELOG

-- 步骤6：配置归档日志目标（可选，但推荐）
ALTER SYSTEM SET log_archive_dest_1='LOCATION=/backup/oracle/archive' SCOPE=SPFILE;
ALTER SYSTEM SET log_archive_format='arch_%t_%s_%r.log' SCOPE=SPFILE;
```

**注意**：切换到归档模式只需要执行一次，之后可以一直使用在线备份。

### 1.3 创建备份目录

```bash
# 创建备份目录结构
mkdir -p /backup/oracle/rman
mkdir -p /backup/oracle/archive
mkdir -p /backup/oracle/rman/logs
mkdir -p /backup/oracle/datapump
mkdir -p /backup/oracle/datapump/logs

# 设置权限
chown oracle:oinstall /backup/oracle -R
chmod 755 /backup/oracle -R
```

### 1.4 在数据库中创建目录对象

```sql
-- 创建RMAN备份目录
CREATE DIRECTORY backup_dir AS '/backup/oracle/rman';
CREATE DIRECTORY archive_dir AS '/backup/oracle/archive';

-- 创建Data Pump目录
CREATE DIRECTORY dpump_dir AS '/backup/oracle/datapump';
CREATE DIRECTORY log_dir AS '/backup/oracle/datapump/logs';

-- 授权
GRANT READ, WRITE ON DIRECTORY backup_dir TO system;
GRANT READ, WRITE ON DIRECTORY archive_dir TO system;
GRANT READ, WRITE ON DIRECTORY dpump_dir TO system;
GRANT READ, WRITE ON DIRECTORY log_dir TO system;

-- 查看目录
SELECT * FROM dba_directories WHERE directory_name IN ('BACKUP_DIR', 'ARCHIVE_DIR', 'DPUMP_DIR', 'LOG_DIR');
```

### 1.5 检查磁盘空间

```bash
# 检查备份目录空间（建议至少是数据库大小的2-3倍）
df -h /backup/oracle

# 检查数据库大小
sqlplus / as sysdba <<EOF
SELECT 
    ROUND(SUM(bytes)/1024/1024/1024, 2) AS "Database Size (GB)"
FROM dba_data_files;
EXIT;
EOF
```

---

## 2. RMAN 在线备份完整流程

### 2.1 配置 RMAN

```bash
# 启动RMAN
rman target /

# 查看当前配置
RMAN> SHOW ALL;

# 配置备份保留策略（保留7天）
RMAN> CONFIGURE RETENTION POLICY TO REDUNDANCY 7;

# 配置默认设备类型
RMAN> CONFIGURE DEFAULT DEVICE TYPE TO DISK;

# 配置备份格式
RMAN> CONFIGURE CHANNEL DEVICE TYPE DISK FORMAT '/backup/oracle/rman/%d_%T_%s_%p.bkp';

# 配置控制文件自动备份（重要！）
RMAN> CONFIGURE CONTROLFILE AUTOBACKUP ON;
RMAN> CONFIGURE CONTROLFILE AUTOBACKUP FORMAT FOR DEVICE TYPE DISK TO '/backup/oracle/rman/control_%F';

# 配置归档日志删除策略
RMAN> CONFIGURE ARCHIVELOG DELETION POLICY TO BACKED UP 1 TIMES TO DISK;

# 查看配置
RMAN> SHOW ALL;
```

### 2.2 完整在线备份流程（推荐）

这是**最完整的备份流程**，确保备份期间的所有数据变化都被捕获：

```bash
rman target / <<EOF
RUN {
  # ========== 第一步：备份当前所有归档日志 ==========
  # 备份备份开始前已存在的所有归档日志
  BACKUP ARCHIVELOG ALL DELETE INPUT;
  
  # ========== 第二步：备份数据库（在线备份） ==========
  # 数据库保持OPEN状态，业务正常运行
  # 备份期间产生的数据变化会被记录到重做日志
  ALLOCATE CHANNEL ch1 TYPE DISK FORMAT '/backup/oracle/rman/full_%d_%T_%s_%p.bkp';
  ALLOCATE CHANNEL ch2 TYPE DISK FORMAT '/backup/oracle/rman/full_%d_%T_%s_%p.bkp';
  
  BACKUP AS COMPRESSED BACKUPSET 
    DATABASE
    TAG 'FULL_BACKUP';
  
  # ========== 第三步：强制切换日志 ==========
  # 将当前重做日志切换，生成归档日志
  # 这样备份期间产生的数据变化就被归档了
  SQL "ALTER SYSTEM ARCHIVE LOG CURRENT";
  
  # ========== 第四步：备份备份期间产生的归档日志 ==========
  # 备份从第二步到第三步之间产生的所有归档日志
  # 这些归档日志包含了备份期间的所有数据变化
  BACKUP ARCHIVELOG ALL DELETE INPUT;
  
  # ========== 第五步：备份控制文件和SPFILE ==========
  BACKUP CURRENT CONTROLFILE;
  BACKUP SPFILE;
  
  # 释放通道
  RELEASE CHANNEL ch1;
  RELEASE CHANNEL ch2;
}
EOF
```

### 2.3 备份流程时间线说明

```
时间轴示例：

10:00:00 - 开始备份
          ├─ 备份归档日志（备份开始前已存在的）
          │
10:00:30 - 开始备份数据库文件
          ├─ 备份数据文件1（状态：10:00:30）
          ├─ 备份数据文件2（状态：10:00:30）
          │
10:01:00 - 备份进行中，用户继续操作
          ├─ 用户插入100条记录 → 写入数据文件1
          ├─ 用户更新50条记录 → 写入数据文件2
          └─ 这些变化记录到重做日志（Redo Log）
          │
10:02:00 - 备份继续
          ├─ 备份数据文件3（状态：10:02:00）
          │
10:03:00 - 数据库备份完成
          │
10:03:10 - 执行：ALTER SYSTEM ARCHIVE LOG CURRENT
          ├─ 强制切换重做日志
          └─ 生成归档日志（包含10:01:00-10:03:10的所有数据变化）
          │
10:03:20 - 备份归档日志
          ├─ 备份包含备份期间数据变化的归档日志
          │
10:03:30 - 备份控制文件和SPFILE
          │
10:03:40 - 备份完成
          │
恢复时：
          ├─ 恢复数据文件（恢复到10:00:30-10:02:00的状态）
          ├─ 应用归档日志（包含10:01:00插入的100条记录和更新的50条记录）
          └─ 数据库恢复到10:03:40的完整状态 ✓
```

### 2.4 增量备份流程

#### 0级增量备份（全量）

```bash
rman target / <<EOF
RUN {
  # 备份归档日志
  BACKUP ARCHIVELOG ALL DELETE INPUT;
  
  # 0级增量备份（相当于全量备份）
  ALLOCATE CHANNEL ch1 TYPE DISK FORMAT '/backup/oracle/rman/level0_%d_%T_%s_%p.bkp';
  BACKUP INCREMENTAL LEVEL 0 
    AS COMPRESSED BACKUPSET 
    DATABASE 
    TAG 'LEVEL0_BACKUP';
  
  # 切换日志
  SQL "ALTER SYSTEM ARCHIVE LOG CURRENT";
  
  # 备份归档日志
  BACKUP ARCHIVELOG ALL DELETE INPUT;
  
  # 备份控制文件
  BACKUP CURRENT CONTROLFILE;
  
  RELEASE CHANNEL ch1;
}
EOF
```

#### 1级增量备份

```bash
rman target / <<EOF
RUN {
  # 备份归档日志
  BACKUP ARCHIVELOG ALL DELETE INPUT;
  
  # 1级增量备份（备份自上次0级或1级备份后的变化）
  ALLOCATE CHANNEL ch1 TYPE DISK FORMAT '/backup/oracle/rman/level1_%d_%T_%s_%p.bkp';
  BACKUP INCREMENTAL LEVEL 1 
    AS COMPRESSED BACKUPSET 
    DATABASE 
    TAG 'LEVEL1_BACKUP';
  
  # 切换日志
  SQL "ALTER SYSTEM ARCHIVE LOG CURRENT";
  
  # 备份归档日志
  BACKUP ARCHIVELOG ALL DELETE INPUT;
  
  # 备份控制文件
  BACKUP CURRENT CONTROLFILE;
  
  RELEASE CHANNEL ch1;
}
EOF
```

### 2.5 查看备份信息

```bash
rman target /

# 查看所有备份
RMAN> LIST BACKUP;

# 查看备份摘要
RMAN> LIST BACKUP SUMMARY;

# 查看数据库备份
RMAN> LIST BACKUP OF DATABASE;

# 查看归档日志备份
RMAN> LIST BACKUP OF ARCHIVELOG ALL;

# 查看控制文件备份
RMAN> LIST BACKUP OF CONTROLFILE;

# 查看需要备份的报告
RMAN> REPORT NEED BACKUP;

# 查看过期备份
RMAN> REPORT OBSOLETE;
```

### 2.6 删除过期备份

```bash
rman target / <<EOF
# 删除过期的备份（根据保留策略）
DELETE OBSOLETE;

# 删除指定日期之前的备份
DELETE BACKUP COMPLETED BEFORE 'SYSDATE-7';

# 删除已备份的归档日志
DELETE ARCHIVELOG ALL BACKED UP 1 TIMES TO DISK;

# 删除7天前的归档日志
DELETE ARCHIVELOG ALL COMPLETED BEFORE 'SYSDATE-7';
EOF
```

---

## 3. 备份期间数据处理机制

### 3.1 核心原理

**RMAN在线备份期间，数据库继续运行，新数据通过归档日志机制被捕获：**

```
备份期间数据变化处理流程：

1. 备份开始时
   └─ 备份当前所有归档日志（备份开始前的数据变化）

2. 备份数据库文件时
   ├─ 数据库保持OPEN状态
   ├─ 用户可以继续读写数据
   └─ 新数据变化写入重做日志（Redo Log）

3. 备份完成后
   ├─ 执行 ALTER SYSTEM ARCHIVE LOG CURRENT
   ├─ 重做日志切换为归档日志
   └─ 归档日志包含备份期间的所有数据变化

4. 备份归档日志
   └─ 备份包含备份期间数据变化的归档日志

5. 恢复时
   ├─ 恢复数据文件（恢复到备份时刻状态）
   ├─ 应用归档日志（恢复备份期间的数据变化）
   └─ 数据库恢复到最新完整状态
```

### 3.2 为什么必须使用 PLUS ARCHIVELOG

```bash
# ❌ 错误做法：只备份数据库
BACKUP DATABASE;
# 问题：备份期间的数据变化会丢失！

# ✅ 正确做法：备份数据库 + 归档日志
BACKUP DATABASE PLUS ARCHIVELOG;
# 或者使用完整流程（推荐）
RUN {
  BACKUP ARCHIVELOG ALL;
  BACKUP DATABASE;
  SQL "ALTER SYSTEM ARCHIVE LOG CURRENT";
  BACKUP ARCHIVELOG ALL;
}
```

### 3.3 验证备份期间数据不丢失

#### 测试场景：

```sql
-- 步骤1：备份前创建测试表
CREATE TABLE backup_test AS SELECT * FROM all_objects WHERE ROWNUM <= 1000;
COMMIT;

-- 步骤2：记录当前记录数
SELECT COUNT(*) AS before_backup FROM backup_test;
-- 假设返回：1000

-- 步骤3：开始备份（在另一个会话）
-- 执行RMAN备份命令

-- 步骤4：备份进行中，插入新数据
INSERT INTO backup_test SELECT * FROM all_objects WHERE ROWNUM <= 500;
COMMIT;
-- 现在有1500条记录

-- 步骤5：备份完成

-- 步骤6：模拟恢复（测试）
-- 恢复数据库后，检查数据
SELECT COUNT(*) AS after_recovery FROM backup_test;
-- 应该返回：1500（包含备份期间插入的500条记录）✓
```

### 3.4 归档日志管理

#### 查看归档日志信息

```sql
-- 查看归档日志
SELECT 
    sequence#,
    first_change#,
    next_change#,
    name,
    archived,
    status,
    completion_time
FROM v$archived_log
ORDER BY sequence# DESC;

-- 查看归档日志位置
SELECT name, value FROM v$parameter WHERE name LIKE 'log_archive_dest%';

-- 查看当前重做日志
SELECT group#, sequence#, status, first_change# FROM v$log;
```

#### 归档日志空间管理

```bash
# 检查归档日志占用空间
du -sh /backup/oracle/archive/*

# 删除已备份的归档日志
rman target / <<EOF
DELETE ARCHIVELOG ALL BACKED UP 1 TIMES TO DISK;
EOF

# 删除指定日期之前的归档日志
rman target / <<EOF
DELETE ARCHIVELOG ALL COMPLETED BEFORE 'SYSDATE-7';
EOF
```

---

## 4. Data Pump 备份流程

### 4.1 全库导出

```bash
# 基本全库导出
expdp system/password@database_name \
  FULL=Y \
  DIRECTORY=dpump_dir \
  DUMPFILE=full_backup_%U.dmp \
  LOGFILE=log_dir:full_backup.log \
  PARALLEL=4

# 压缩导出（推荐）
expdp system/password@database_name \
  FULL=Y \
  DIRECTORY=dpump_dir \
  DUMPFILE=full_backup_%U.dmp \
  LOGFILE=log_dir:full_backup.log \
  COMPRESSION=ALL \
  PARALLEL=4 \
  FILESIZE=2G
```

### 4.2 按用户导出

```bash
expdp system/password@database_name \
  SCHEMAS=user1,user2 \
  DIRECTORY=dpump_dir \
  DUMPFILE=schema_backup_%U.dmp \
  LOGFILE=log_dir:schema_backup.log \
  COMPRESSION=ALL \
  PARALLEL=4
```

### 4.3 按表导出

```bash
expdp system/password@database_name \
  TABLES=user1.table1,user1.table2 \
  DIRECTORY=dpump_dir \
  DUMPFILE=table_backup.dmp \
  LOGFILE=log_dir:table_backup.log
```

### 4.4 查看导出进度

```sql
-- 在另一个会话中查看导出进度
SELECT 
    job_name,
    state,
    degree,
    attached_sessions,
    total_bytes,
    bytes_processed,
    ROUND(bytes_processed/total_bytes*100, 2) AS progress_pct
FROM dba_datapump_jobs
WHERE state = 'EXECUTING';
```

### 4.5 导入操作

```bash
# 全库导入
impdp system/password@database_name \
  FULL=Y \
  DIRECTORY=dpump_dir \
  DUMPFILE=full_backup_%U.dmp \
  LOGFILE=log_dir:full_import.log \
  PARALLEL=4

# 按用户导入
impdp system/password@database_name \
  SCHEMAS=user1 \
  DIRECTORY=dpump_dir \
  DUMPFILE=schema_backup.dmp \
  LOGFILE=log_dir:schema_import.log \
  TABLE_EXISTS_ACTION=REPLACE
```

---

## 5. 备份验证与恢复

### 5.1 验证备份完整性

```bash
rman target / <<EOF
# 验证所有备份集
VALIDATE BACKUPSET ALL;

# 验证数据库文件
RESTORE DATABASE VALIDATE;

# 验证归档日志
RESTORE ARCHIVELOG ALL VALIDATE;

# 查看验证结果
LIST BACKUP;
EOF
```

### 5.2 测试恢复（不实际恢复）

```bash
rman target / <<EOF
# 测试恢复数据库（不实际恢复）
RESTORE DATABASE VALIDATE;
RESTORE ARCHIVELOG ALL VALIDATE;
EOF
```

### 5.3 完整恢复流程

#### 场景1：恢复整个数据库

```bash
rman target / <<EOF
# 步骤1：关闭数据库
SHUTDOWN IMMEDIATE;

# 步骤2：启动到mount状态
STARTUP MOUNT;

# 步骤3：恢复数据文件
RESTORE DATABASE;

# 步骤4：应用归档日志（恢复备份期间的数据变化）
RECOVER DATABASE;

# 步骤5：打开数据库
ALTER DATABASE OPEN;
EOF
```

#### 场景2：时间点恢复（PITR）

```bash
rman target / <<EOF
# 恢复到指定时间点
RUN {
  SHUTDOWN IMMEDIATE;
  STARTUP MOUNT;
  
  # 恢复到指定时间点
  SET UNTIL TIME "TO_DATE('2024-01-15 14:30:00', 'YYYY-MM-DD HH24:MI:SS')";
  
  RESTORE DATABASE;
  RECOVER DATABASE;
  ALTER DATABASE OPEN RESETLOGS;
}
EOF
```

#### 场景3：恢复表空间

```bash
rman target / <<EOF
# 步骤1：将表空间离线
SQL "ALTER TABLESPACE users OFFLINE IMMEDIATE";

# 步骤2：恢复表空间
RESTORE TABLESPACE users;
RECOVER TABLESPACE users;

# 步骤3：将表空间在线
SQL "ALTER TABLESPACE users ONLINE";
EOF
```

#### 场景4：恢复数据文件

```bash
rman target / <<EOF
# 步骤1：将数据文件离线
SQL "ALTER DATABASE DATAFILE '/path/to/datafile.dbf' OFFLINE";

# 步骤2：恢复数据文件
RESTORE DATAFILE '/path/to/datafile.dbf';
RECOVER DATAFILE '/path/to/datafile.dbf';

# 步骤3：将数据文件在线
SQL "ALTER DATABASE DATAFILE '/path/to/datafile.dbf' ONLINE";
EOF
```

---

## 6. 自动化备份方案

### 6.1 完整RMAN自动备份脚本

```bash
#!/bin/bash
# Oracle RMAN 完整自动备份脚本
# 文件名: oracle_rman_full_backup.sh

# ========== 配置变量 ==========
ORACLE_SID=ORCL
ORACLE_HOME=/u01/app/oracle/product/19.0.0/dbhome_1
BACKUP_BASE=/backup/oracle/rman
LOG_DIR=/backup/oracle/rman/logs
RETENTION_DAYS=7
DATE=$(date +%Y%m%d_%H%M%S)
DAY_OF_WEEK=$(date +%u)  # 1=Monday, 7=Sunday

# ========== 设置环境变量 ==========
export ORACLE_SID
export ORACLE_HOME
export PATH=$ORACLE_HOME/bin:$PATH

# ========== 创建目录 ==========
mkdir -p $LOG_DIR

# ========== 日志函数 ==========
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a $LOG_DIR/backup_${DATE}.log
}

# ========== 错误处理函数 ==========
error_exit() {
    log "错误: $1"
    log "========== 备份失败 =========="
    exit 1
}

# ========== 开始备份 ==========
log "========== 开始 RMAN 备份 =========="
log "备份时间: $(date)"
log "数据库: $ORACLE_SID"
log "备份目录: $BACKUP_BASE"
log "星期: $DAY_OF_WEEK (1=周一, 7=周日)"

# ========== 检查数据库状态 ==========
log "检查数据库状态..."
DB_STATUS=$(sqlplus -s / as sysdba <<EOF
SET PAGESIZE 0 FEEDBACK OFF VERIFY OFF HEADING OFF ECHO OFF
SELECT status FROM v\$instance;
EXIT;
EOF
)

if [ "$DB_STATUS" != "OPEN" ]; then
    error_exit "数据库状态不是OPEN，当前状态: $DB_STATUS"
fi

log "数据库状态: $DB_STATUS ✓"

# ========== 检查归档模式 ==========
log "检查归档模式..."
ARCH_MODE=$(sqlplus -s / as sysdba <<EOF
SET PAGESIZE 0 FEEDBACK OFF VERIFY OFF HEADING OFF ECHO OFF
SELECT log_mode FROM v\$database;
EXIT;
EOF
)

if [ "$ARCH_MODE" != "ARCHIVELOG" ]; then
    error_exit "数据库不是归档模式，当前模式: $ARCH_MODE"
fi

log "归档模式: $ARCH_MODE ✓"

# ========== 检查磁盘空间 ==========
log "检查磁盘空间..."
AVAILABLE_SPACE=$(df -h $BACKUP_BASE | awk 'NR==2 {print $4}')
log "可用空间: $AVAILABLE_SPACE"

# ========== 执行备份 ==========
log "开始执行备份..."

# 每周日执行0级增量（全量），其他时间执行1级增量
if [ $DAY_OF_WEEK -eq 7 ]; then
    BACKUP_LEVEL=0
    BACKUP_TYPE="0级增量（全量）"
    BACKUP_TAG="LEVEL0_${DATE}"
else
    BACKUP_LEVEL=1
    BACKUP_TYPE="1级增量"
    BACKUP_TAG="LEVEL1_${DATE}"
fi

log "备份类型: $BACKUP_TYPE"

# 执行RMAN备份
rman target / <<EOF | tee -a $LOG_DIR/backup_${DATE}.log
RUN {
  # 备份当前所有归档日志
  BACKUP ARCHIVELOG ALL DELETE INPUT;
  
  # 分配通道
  ALLOCATE CHANNEL ch1 TYPE DISK FORMAT '$BACKUP_BASE/${BACKUP_TAG}_%d_%T_%s_%p.bkp';
  ALLOCATE CHANNEL ch2 TYPE DISK FORMAT '$BACKUP_BASE/${BACKUP_TAG}_%d_%T_%s_%p.bkp';
  
  # 执行备份
  BACKUP INCREMENTAL LEVEL ${BACKUP_LEVEL}
    AS COMPRESSED BACKUPSET 
    DATABASE 
    TAG '${BACKUP_TAG}';
  
  # 强制切换日志，确保备份期间的数据变化被归档
  SQL "ALTER SYSTEM ARCHIVE LOG CURRENT";
  
  # 备份备份期间产生的归档日志
  BACKUP ARCHIVELOG ALL DELETE INPUT;
  
  # 备份控制文件和SPFILE
  BACKUP CURRENT CONTROLFILE;
  BACKUP SPFILE;
  
  # 释放通道
  RELEASE CHANNEL ch1;
  RELEASE CHANNEL ch2;
}
EXIT;
EOF

# ========== 检查备份结果 ==========
if [ $? -eq 0 ]; then
    log "备份命令执行成功"
    
    # 验证备份
    log "验证备份完整性..."
    rman target / <<EOF | tee -a $LOG_DIR/backup_${DATE}.log
    VALIDATE BACKUPSET ALL;
    EXIT;
EOF
    
    if [ $? -eq 0 ]; then
        log "备份验证通过 ✓"
    else
        log "警告: 备份验证失败，请检查备份文件"
    fi
    
    # 删除过期备份
    log "清理过期备份（保留 $RETENTION_DAYS 天）..."
    rman target / <<EOF | tee -a $LOG_DIR/backup_${DATE}.log
    DELETE OBSOLETE;
    DELETE ARCHIVELOG ALL BACKED UP 1 TIMES TO DISK;
    EXIT;
EOF
    
    # 生成备份报告
    log "生成备份报告..."
    rman target / <<EOF | tee -a $LOG_DIR/backup_${DATE}.log
    LIST BACKUP SUMMARY;
    REPORT OBSOLETE;
    EXIT;
EOF
    
    log "========== 备份完成 =========="
    log "备份日志: $LOG_DIR/backup_${DATE}.log"
    
    # 发送成功通知（可选）
    # echo "Oracle备份成功完成 - $DATE" | mail -s "备份成功" admin@example.com
    
    exit 0
else
    error_exit "备份命令执行失败"
fi
```

### 6.2 设置执行权限

```bash
chmod +x oracle_rman_full_backup.sh
```

### 6.3 配置定时任务

```bash
# 编辑 crontab
crontab -e

# 添加定时任务
# 每周日凌晨3点执行0级增量备份（全量）
0 3 * * 0 /backup/oracle/scripts/oracle_rman_full_backup.sh >> /backup/oracle/rman/logs/cron.log 2>&1

# 周一到周六凌晨2点执行1级增量备份
0 2 * * 1-6 /backup/oracle/scripts/oracle_rman_full_backup.sh >> /backup/oracle/rman/logs/cron.log 2>&1
```

### 6.4 Data Pump 自动备份脚本

```bash
#!/bin/bash
# Oracle Data Pump 自动备份脚本
# 文件名: oracle_datapump_backup.sh

ORACLE_SID=ORCL
ORACLE_HOME=/u01/app/oracle/product/19.0.0/dbhome_1
DB_USER=system
DB_PASSWORD=password
DB_NAME=ORCL
BACKUP_DIR=/backup/oracle/datapump
LOG_DIR=/backup/oracle/datapump/logs
RETENTION_DAYS=7
DATE=$(date +%Y%m%d_%H%M%S)

export ORACLE_SID
export ORACLE_HOME
export PATH=$ORACLE_HOME/bin:$PATH

mkdir -p $BACKUP_DIR
mkdir -p $LOG_DIR

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a $LOG_DIR/datapump_${DATE}.log
}

log "========== 开始 Data Pump 备份 =========="
log "备份时间: $(date)"
log "数据库: $DB_NAME"

# 执行全库导出
expdp ${DB_USER}/${DB_PASSWORD}@${DB_NAME} \
  FULL=Y \
  DIRECTORY=dpump_dir \
  DUMPFILE=full_backup_${DATE}_%U.dmp \
  LOGFILE=log_dir:full_backup_${DATE}.log \
  COMPRESSION=ALL \
  PARALLEL=4 \
  FILESIZE=2G

if [ $? -eq 0 ]; then
    log "备份成功完成"
    
    # 压缩备份文件（可选）
    log "压缩备份文件..."
    gzip $BACKUP_DIR/*.dmp 2>/dev/null
    
    # 删除过期备份
    log "清理过期备份（保留 $RETENTION_DAYS 天）..."
    find $BACKUP_DIR -name "*.dmp.gz" -mtime +$RETENTION_DAYS -delete
    find $LOG_DIR -name "*.log" -mtime +$RETENTION_DAYS -delete
    
    log "========== 备份完成 =========="
    exit 0
else
    log "备份失败！"
    log "========== 备份失败 =========="
    exit 1
fi
```

---

## 7. 常见问题处理

### 7.1 备份空间不足

```bash
# 检查空间
df -h /backup/oracle

# 删除过期备份
rman target / <<EOF
DELETE OBSOLETE;
EXIT;
EOF

# 使用压缩备份
rman target / <<EOF
BACKUP AS COMPRESSED BACKUPSET DATABASE;
EXIT;
EOF
```

### 7.2 归档日志空间不足

```bash
# 检查归档日志
rman target / <<EOF
LIST ARCHIVELOG ALL;
EXIT;
EOF

# 删除已备份的归档日志
rman target / <<EOF
DELETE ARCHIVELOG ALL BACKED UP 1 TIMES TO DISK;
EXIT;
EOF

# 配置归档日志自动删除策略
rman target / <<EOF
CONFIGURE ARCHIVELOG DELETION POLICY TO BACKED UP 1 TIMES TO DISK;
EXIT;
EOF
```

### 7.3 备份失败

```bash
# 查看RMAN日志
tail -f $ORACLE_HOME/diag/rdbms/$ORACLE_SID/$ORACLE_SID/trace/alert_$ORACLE_SID.log

# 查看备份配置
rman target / <<EOF
SHOW ALL;
EXIT;
EOF

# 检查数据库状态
sqlplus / as sysdba <<EOF
SELECT status FROM v\$instance;
SELECT log_mode FROM v\$database;
EXIT;
EOF
```

### 7.4 Data Pump 作业挂起

```sql
-- 查看作业状态
SELECT 
    job_name, 
    state, 
    degree, 
    attached_sessions,
    start_time,
    last_update_time
FROM dba_datapump_jobs;

-- 停止作业
BEGIN
  DBMS_DATAPUMP.STOP_JOB('JOB_NAME', 'IMMEDIATE');
END;
/

-- 删除作业
BEGIN
  DBMS_DATAPUMP.DETACH('JOB_NAME');
END;
/
```

### 7.5 恢复时找不到归档日志

```bash
# 检查归档日志位置
rman target / <<EOF
LIST ARCHIVELOG ALL;
EXIT;
EOF

# 注册归档日志
rman target / <<EOF
CATALOG ARCHIVELOG '/path/to/archive_log.log';
EXIT;
EOF

# 或者指定归档日志目录
rman target / <<EOF
SET ARCHIVELOG DESTINATION TO '/backup/oracle/archive';
RECOVER DATABASE;
EXIT;
EOF
```

---

## 8. 备份策略建议

### 8.1 生产环境备份策略

| 备份类型 | 频率 | 保留时间 | 执行时间 | 说明 |
|---------|------|---------|---------|------|
| 0级增量（全量） | 每周1次 | 4周 | 周日凌晨3点 | 完整备份 |
| 1级增量 | 每天1次 | 2周 | 周一至周六凌晨2点 | 增量备份 |
| 归档日志 | 每小时或实时 | 1周 | 自动 | 捕获所有数据变化 |
| Data Pump | 每周1次 | 2周 | 周日晚上 | 逻辑备份，用于迁移 |

### 8.2 备份检查清单

- [ ] 数据库处于归档模式（ARCHIVELOG）
- [ ] 备份脚本已配置并测试
- [ ] 定时任务已设置
- [ ] 备份目录空间充足（至少数据库大小的2-3倍）
- [ ] 备份文件权限正确
- [ ] 备份验证定期执行
- [ ] 恢复测试定期执行（至少每季度一次）
- [ ] 备份日志定期检查
- [ ] 异地备份已配置
- [ ] 备份监控告警已配置

### 8.3 备份监控

```bash
# 检查最近备份
rman target / <<EOF
LIST BACKUP SUMMARY;
EXIT;
EOF

# 检查备份作业
sqlplus / as sysdba <<EOF
SELECT 
    job_name, 
    state, 
    start_time, 
    end_time,
    ROUND((end_time - start_time) * 24 * 60, 2) AS duration_minutes
FROM dba_datapump_jobs 
ORDER BY start_time DESC;
EXIT;
EOF

# 检查备份目录大小
du -sh /backup/oracle/*

# 检查备份文件数量
find /backup/oracle/rman -name "*.bkp" | wc -l
```

---

## 9. 关键要点总结

### 9.1 在线备份要点

1. **必须启用归档模式**：在线备份的前提条件
2. **必须备份归档日志**：使用 `PLUS ARCHIVELOG` 或完整流程
3. **备份期间数据不丢失**：通过归档日志机制保证
4. **无需停机**：数据库保持OPEN状态，业务正常运行

### 9.2 完整备份流程要点

```
完整备份流程（确保数据不丢失）：

1. 备份当前归档日志（备份开始前的数据变化）
2. 备份数据库文件（在线备份）
3. 切换日志（ALTER SYSTEM ARCHIVE LOG CURRENT）
4. 备份备份期间产生的归档日志（备份期间的数据变化）
5. 备份控制文件和SPFILE
```

### 9.3 恢复要点

```
恢复流程：

1. 恢复数据文件（RESTORE DATABASE）
2. 应用归档日志（RECOVER DATABASE）
   └─ 包含备份期间的所有数据变化
3. 打开数据库（ALTER DATABASE OPEN）
```

---

**文档版本**: v2.0  
**最后更新**: 2024-01-XX  
**适用版本**: Oracle 11g/12c/19c/21c  
**关键特性**: 在线备份、数据不丢失、完整流程

