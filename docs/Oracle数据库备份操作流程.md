# Oracle 数据库备份操作流程详细指南

## 目录
1. [RMAN 备份操作流程](#1-rman-备份操作流程)
2. [Data Pump 备份操作流程](#2-data-pump-备份操作流程)
3. [传统 exp/imp 备份操作流程](#3-传统-expexpdp-备份操作流程)
4. [备份验证与恢复测试](#4-备份验证与恢复测试)
5. [自动化备份脚本](#5-自动化备份脚本)
6. [常见问题处理](#6-常见问题处理)

---

## 1. RMAN 备份操作流程

### 1.1 环境准备

#### 步骤1：检查 Oracle 环境
```bash
# 检查 Oracle 版本
sqlplus / as sysdba <<EOF
SELECT * FROM v\$version;
EXIT;
EOF

# 检查数据库状态
sqlplus / as sysdba <<EOF
SELECT status FROM v\$instance;
EXIT;
EOF
```

#### 步骤2：创建备份目录
```bash
# 在操作系统创建备份目录
mkdir -p /backup/oracle/rman
mkdir -p /backup/oracle/archive

# 设置目录权限
chown oracle:oinstall /backup/oracle -R
chmod 755 /backup/oracle -R
```

#### 步骤3：在数据库中创建备份目录对象
```sql
-- 连接到数据库
sqlplus / as sysdba

-- 创建备份目录（如果不存在）
CREATE DIRECTORY backup_dir AS '/backup/oracle/rman';
CREATE DIRECTORY archive_dir AS '/backup/oracle/archive';

-- 授权给备份用户
GRANT READ, WRITE ON DIRECTORY backup_dir TO system;
GRANT READ, WRITE ON DIRECTORY archive_dir TO system;

-- 查看目录
SELECT * FROM dba_directories WHERE directory_name IN ('BACKUP_DIR', 'ARCHIVE_DIR');
```

### 1.2 配置 RMAN

#### 步骤1：检查 RMAN 配置
```bash
# 启动 RMAN
rman target /

# 查看当前配置
RMAN> SHOW ALL;

# 配置备份保留策略（保留7天）
RMAN> CONFIGURE RETENTION POLICY TO REDUNDANCY 7;

# 配置备份设备类型
RMAN> CONFIGURE DEFAULT DEVICE TYPE TO DISK;

# 配置备份格式
RMAN> CONFIGURE CHANNEL DEVICE TYPE DISK FORMAT '/backup/oracle/rman/%d_%T_%s_%p.bkp';

# 配置控制文件自动备份
RMAN> CONFIGURE CONTROLFILE AUTOBACKUP ON;
RMAN> CONFIGURE CONTROLFILE AUTOBACKUP FORMAT FOR DEVICE TYPE DISK TO '/backup/oracle/rman/control_%F';

# 配置归档日志删除策略
RMAN> CONFIGURE ARCHIVELOG DELETION POLICY TO BACKED UP 1 TIMES TO DISK;
```

#### 步骤2：验证配置
```bash
RMAN> SHOW ALL;
```

### 1.3 执行全量备份

#### 方法1：简单全量备份
```bash
# 连接到 RMAN
rman target /

# 执行全量备份
RMAN> BACKUP DATABASE;

# 备份完成后查看备份信息
RMAN> LIST BACKUP SUMMARY;
```

#### 方法2：完整备份（包含归档日志）
```bash
rman target / <<EOF
RUN {
  ALLOCATE CHANNEL ch1 TYPE DISK FORMAT '/backup/oracle/rman/full_%d_%T_%s_%p.bkp';
  BACKUP DATABASE PLUS ARCHIVELOG;
  BACKUP CURRENT CONTROLFILE;
  RELEASE CHANNEL ch1;
}
EOF
```

#### 方法3：压缩备份（节省空间）
```bash
rman target / <<EOF
RUN {
  ALLOCATE CHANNEL ch1 TYPE DISK FORMAT '/backup/oracle/rman/full_%d_%T_%s_%p.bkp';
  BACKUP AS COMPRESSED BACKUPSET DATABASE PLUS ARCHIVELOG;
  BACKUP CURRENT CONTROLFILE;
  RELEASE CHANNEL ch1;
}
EOF
```

### 1.4 执行增量备份

#### 步骤1：0级增量备份（全量）
```bash
rman target / <<EOF
RUN {
  ALLOCATE CHANNEL ch1 TYPE DISK FORMAT '/backup/oracle/rman/level0_%d_%T_%s_%p.bkp';
  BACKUP INCREMENTAL LEVEL 0 DATABASE PLUS ARCHIVELOG;
  RELEASE CHANNEL ch1;
}
EOF
```

#### 步骤2：1级增量备份
```bash
rman target / <<EOF
RUN {
  ALLOCATE CHANNEL ch1 TYPE DISK FORMAT '/backup/oracle/rman/level1_%d_%T_%s_%p.bkp';
  BACKUP INCREMENTAL LEVEL 1 DATABASE PLUS ARCHIVELOG;
  RELEASE CHANNEL ch1;
}
EOF
```

#### 步骤3：差异增量备份（推荐）
```bash
# 差异增量：备份自上次0级或1级备份后的所有变化
rman target / <<EOF
RUN {
  ALLOCATE CHANNEL ch1 TYPE DISK FORMAT '/backup/oracle/rman/diff_%d_%T_%s_%p.bkp';
  BACKUP INCREMENTAL LEVEL 1 CUMULATIVE DATABASE PLUS ARCHIVELOG;
  RELEASE CHANNEL ch1;
}
EOF
```

### 1.5 备份表空间

```bash
rman target / <<EOF
RUN {
  ALLOCATE CHANNEL ch1 TYPE DISK FORMAT '/backup/oracle/rman/tablespace_%d_%T_%s_%p.bkp';
  BACKUP TABLESPACE users, system;
  RELEASE CHANNEL ch1;
}
EOF
```

### 1.6 备份数据文件

```bash
# 查看数据文件
sqlplus / as sysdba <<EOF
SELECT file_name FROM dba_data_files;
EXIT;
EOF

# 备份指定数据文件
rman target / <<EOF
RUN {
  ALLOCATE CHANNEL ch1 TYPE DISK FORMAT '/backup/oracle/rman/datafile_%d_%T_%s_%p.bkp';
  BACKUP DATAFILE 1, 2, 3;
  RELEASE CHANNEL ch1;
}
EOF
```

### 1.7 查看备份信息

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

# 查看备份报告
RMAN> REPORT NEED BACKUP;
RMAN> REPORT OBSOLETE;
```

### 1.8 删除过期备份

```bash
rman target / <<EOF
# 删除过期的备份（根据保留策略）
DELETE OBSOLETE;

# 删除指定日期之前的备份
DELETE BACKUP COMPLETED BEFORE 'SYSDATE-7';

# 删除指定的备份集
DELETE BACKUPSET <backupset_key>;

# 删除归档日志（已备份的）
DELETE ARCHIVELOG ALL BACKED UP 1 TIMES TO DISK;
EOF
```

---

## 2. Data Pump 备份操作流程

### 2.1 环境准备

#### 步骤1：创建目录对象
```sql
-- 连接到数据库
sqlplus / as sysdba

-- 创建导出目录
CREATE DIRECTORY dpump_dir AS '/backup/oracle/datapump';
CREATE DIRECTORY log_dir AS '/backup/oracle/datapump/logs';

-- 授权
GRANT READ, WRITE ON DIRECTORY dpump_dir TO system;
GRANT READ, WRITE ON DIRECTORY log_dir TO system;
GRANT EXP_FULL_DATABASE TO system;  -- 导出权限
GRANT IMP_FULL_DATABASE TO system;  -- 导入权限

-- 查看目录
SELECT * FROM dba_directories WHERE directory_name LIKE 'DPUMP%';
```

#### 步骤2：创建操作系统目录
```bash
mkdir -p /backup/oracle/datapump
mkdir -p /backup/oracle/datapump/logs
chown oracle:oinstall /backup/oracle/datapump -R
chmod 755 /backup/oracle/datapump -R
```

### 2.2 全库导出

#### 步骤1：执行全库导出
```bash
# 基本全库导出
expdp system/password@database_name \
  FULL=Y \
  DIRECTORY=dpump_dir \
  DUMPFILE=full_backup_%U.dmp \
  LOGFILE=log_dir:full_backup.log \
  PARALLEL=4

# 压缩导出（节省空间）
expdp system/password@database_name \
  FULL=Y \
  DIRECTORY=dpump_dir \
  DUMPFILE=full_backup_%U.dmp \
  LOGFILE=log_dir:full_backup.log \
  COMPRESSION=ALL \
  PARALLEL=4

# 导出并指定文件大小（大数据库）
expdp system/password@database_name \
  FULL=Y \
  DIRECTORY=dpump_dir \
  DUMPFILE=full_backup_%U.dmp \
  FILESIZE=2G \
  LOGFILE=log_dir:full_backup.log \
  COMPRESSION=ALL \
  PARALLEL=4
```

#### 步骤2：查看导出进度
```sql
-- 在另一个会话中查看导出进度
sqlplus / as sysdba

SELECT job_name, state, degree, attached_sessions 
FROM dba_datapump_jobs;

-- 查看详细信息
SELECT * FROM dba_datapump_jobs;
```

### 2.3 按用户导出

```bash
# 导出指定用户
expdp system/password@database_name \
  SCHEMAS=user1,user2 \
  DIRECTORY=dpump_dir \
  DUMPFILE=schema_backup_%U.dmp \
  LOGFILE=log_dir:schema_backup.log \
  COMPRESSION=ALL

# 导出用户（包含依赖对象）
expdp system/password@database_name \
  SCHEMAS=user1 \
  DIRECTORY=dpump_dir \
  DUMPFILE=user1_backup.dmp \
  LOGFILE=log_dir:user1_backup.log \
  INCLUDE=TABLE,INDEX,CONSTRAINT,PROCEDURE,FUNCTION,PACKAGE
```

### 2.4 按表导出

```bash
# 导出指定表
expdp system/password@database_name \
  TABLES=user1.table1,user1.table2 \
  DIRECTORY=dpump_dir \
  DUMPFILE=table_backup.dmp \
  LOGFILE=log_dir:table_backup.log

# 导出表（带条件）
expdp system/password@database_name \
  TABLES=user1.table1 \
  QUERY=user1.table1:"WHERE create_date > SYSDATE-30" \
  DIRECTORY=dpump_dir \
  DUMPFILE=table_backup_filtered.dmp \
  LOGFILE=log_dir:table_backup.log
```

### 2.5 按表空间导出

```bash
expdp system/password@database_name \
  TABLESPACES=users,example \
  DIRECTORY=dpump_dir \
  DUMPFILE=tablespace_backup_%U.dmp \
  LOGFILE=log_dir:tablespace_backup.log \
  COMPRESSION=ALL
```

### 2.6 导出元数据

```bash
# 只导出结构（不导出数据）
expdp system/password@database_name \
  SCHEMAS=user1 \
  DIRECTORY=dpump_dir \
  DUMPFILE=schema_structure.dmp \
  LOGFILE=log_dir:schema_structure.log \
  CONTENT=METADATA_ONLY

# 只导出数据（不导出结构）
expdp system/password@database_name \
  SCHEMAS=user1 \
  DIRECTORY=dpump_dir \
  DUMPFILE=schema_data.dmp \
  LOGFILE=log_dir:schema_data.log \
  CONTENT=DATA_ONLY
```

### 2.7 导出时排除对象

```bash
# 排除指定表
expdp system/password@database_name \
  SCHEMAS=user1 \
  DIRECTORY=dpump_dir \
  DUMPFILE=schema_backup.dmp \
  LOGFILE=log_dir:schema_backup.log \
  EXCLUDE=TABLE:"IN ('TEMP_TABLE1','TEMP_TABLE2')"

# 排除指定类型
expdp system/password@database_name \
  SCHEMAS=user1 \
  DIRECTORY=dpump_dir \
  DUMPFILE=schema_backup.dmp \
  LOGFILE=log_dir:schema_backup.log \
  EXCLUDE=INDEX,TRIGGER
```

### 2.8 使用参数文件

#### 步骤1：创建参数文件
```bash
# 创建参数文件 expdp_full.par
cat > /backup/oracle/datapump/expdp_full.par <<EOF
FULL=Y
DIRECTORY=dpump_dir
DUMPFILE=full_backup_%U.dmp
LOGFILE=log_dir:full_backup.log
COMPRESSION=ALL
PARALLEL=4
FILESIZE=2G
EOF
```

#### 步骤2：使用参数文件执行导出
```bash
expdp system/password@database_name PARFILE=/backup/oracle/datapump/expdp_full.par
```

### 2.9 导入操作

#### 全库导入
```bash
# 基本导入
impdp system/password@database_name \
  FULL=Y \
  DIRECTORY=dpump_dir \
  DUMPFILE=full_backup_%U.dmp \
  LOGFILE=log_dir:full_import.log \
  PARALLEL=4

# 导入到新数据库（重映射）
impdp system/password@target_database \
  FULL=Y \
  DIRECTORY=dpump_dir \
  DUMPFILE=full_backup_%U.dmp \
  LOGFILE=log_dir:full_import.log \
  REMAP_SCHEMA=source_user:target_user \
  REMAP_TABLESPACE=source_tbs:target_tbs
```

#### 按用户导入
```bash
impdp system/password@database_name \
  SCHEMAS=user1 \
  DIRECTORY=dpump_dir \
  DUMPFILE=schema_backup.dmp \
  LOGFILE=log_dir:schema_import.log \
  TABLE_EXISTS_ACTION=REPLACE
```

#### 按表导入
```bash
impdp system/password@database_name \
  TABLES=user1.table1 \
  DIRECTORY=dpump_dir \
  DUMPFILE=table_backup.dmp \
  LOGFILE=log_dir:table_import.log \
  TABLE_EXISTS_ACTION=REPLACE
```

#### 导入时转换
```bash
impdp system/password@database_name \
  SCHEMAS=user1 \
  DIRECTORY=dpump_dir \
  DUMPFILE=schema_backup.dmp \
  LOGFILE=log_dir:schema_import.log \
  REMAP_SCHEMA=user1:user2 \
  REMAP_TABLESPACE=users:new_users \
  TRANSFORM=SEGMENT_ATTRIBUTES:N
```

---

## 3. 传统 exp/imp 备份操作流程

### 3.1 全库导出

```bash
# 全库导出
exp system/password@database_name \
  FILE=/backup/oracle/exp/full_backup.dmp \
  LOG=/backup/oracle/exp/full_backup.log \
  FULL=Y \
  COMPRESS=Y

# 导出指定用户
exp system/password@database_name \
  FILE=/backup/oracle/exp/user_backup.dmp \
  LOG=/backup/oracle/exp/user_backup.log \
  OWNER=user1,user2 \
  COMPRESS=Y

# 导出指定表
exp system/password@database_name \
  FILE=/backup/oracle/exp/table_backup.dmp \
  LOG=/backup/oracle/exp/table_backup.log \
  TABLES=user1.table1,user1.table2 \
  COMPRESS=Y
```

### 3.2 全库导入

```bash
# 全库导入
imp system/password@database_name \
  FILE=/backup/oracle/exp/full_backup.dmp \
  LOG=/backup/oracle/exp/full_import.log \
  FULL=Y \
  IGNORE=Y

# 导入指定用户
imp system/password@database_name \
  FILE=/backup/oracle/exp/user_backup.dmp \
  LOG=/backup/oracle/exp/user_import.log \
  FROMUSER=user1 TOUSER=user2 \
  IGNORE=Y
```

---

## 4. 备份验证与恢复测试

### 4.1 RMAN 备份验证

#### 步骤1：验证备份集
```bash
rman target /

# 验证所有备份
RMAN> VALIDATE BACKUPSET ALL;

# 验证指定备份集
RMAN> VALIDATE BACKUPSET <backupset_key>;

# 验证数据库文件
RMAN> RESTORE DATABASE VALIDATE;

# 验证归档日志
RMAN> RESTORE ARCHIVELOG ALL VALIDATE;
```

#### 步骤2：测试恢复（不实际恢复）
```bash
rman target / <<EOF
# 测试恢复数据库（不实际恢复）
RESTORE DATABASE VALIDATE;
RESTORE ARCHIVELOG ALL VALIDATE;
EOF
```

### 4.2 Data Pump 备份验证

```bash
# 检查导出文件
ls -lh /backup/oracle/datapump/*.dmp

# 查看导出日志
cat /backup/oracle/datapump/logs/*.log

# 检查导出文件完整性（使用 SQL）
sqlplus / as sysdba <<EOF
SELECT * FROM dba_datapump_jobs WHERE state = 'EXECUTING';
EXIT;
EOF
```

### 4.3 恢复测试流程

#### 场景1：表空间恢复
```bash
rman target / <<EOF
# 1. 将表空间离线
SQL "ALTER TABLESPACE users OFFLINE IMMEDIATE";

# 2. 恢复表空间
RESTORE TABLESPACE users;
RECOVER TABLESPACE users;

# 3. 将表空间在线
SQL "ALTER TABLESPACE users ONLINE";
EOF
```

#### 场景2：数据文件恢复
```bash
rman target / <<EOF
# 1. 将数据文件离线
SQL "ALTER DATABASE DATAFILE '/path/to/datafile.dbf' OFFLINE";

# 2. 恢复数据文件
RESTORE DATAFILE '/path/to/datafile.dbf';
RECOVER DATAFILE '/path/to/datafile.dbf';

# 3. 将数据文件在线
SQL "ALTER DATABASE DATAFILE '/path/to/datafile.dbf' ONLINE";
EOF
```

#### 场景3：时间点恢复（PITR）
```bash
rman target / <<EOF
# 恢复到指定时间点
RUN {
  SET UNTIL TIME "TO_DATE('2024-01-15 10:00:00', 'YYYY-MM-DD HH24:MI:SS')";
  RESTORE DATABASE;
  RECOVER DATABASE;
  ALTER DATABASE OPEN RESETLOGS;
}
EOF
```

---

## 5. 自动化备份脚本

### 5.1 RMAN 自动化备份脚本

#### 创建备份脚本
```bash
#!/bin/bash
# Oracle RMAN 自动备份脚本
# 文件名: oracle_rman_backup.sh

# 配置变量
ORACLE_SID=ORCL
ORACLE_HOME=/u01/app/oracle/product/19.0.0/dbhome_1
BACKUP_BASE=/backup/oracle/rman
LOG_DIR=/backup/oracle/rman/logs
RETENTION_DAYS=7
DATE=$(date +%Y%m%d_%H%M%S)

# 设置环境变量
export ORACLE_SID
export ORACLE_HOME
export PATH=$ORACLE_HOME/bin:$PATH

# 创建日志目录
mkdir -p $LOG_DIR

# 日志函数
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a $LOG_DIR/backup_${DATE}.log
}

# 开始备份
log "========== 开始 RMAN 备份 =========="
log "备份时间: $(date)"
log "数据库: $ORACLE_SID"
log "备份目录: $BACKUP_BASE"

# 执行全量备份
rman target / <<EOF | tee -a $LOG_DIR/backup_${DATE}.log
RUN {
  ALLOCATE CHANNEL ch1 TYPE DISK FORMAT '$BACKUP_BASE/full_%d_%T_%s_%p.bkp';
  ALLOCATE CHANNEL ch2 TYPE DISK FORMAT '$BACKUP_BASE/full_%d_%T_%s_%p.bkp';
  BACKUP AS COMPRESSED BACKUPSET DATABASE PLUS ARCHIVELOG;
  BACKUP CURRENT CONTROLFILE FORMAT '$BACKUP_BASE/control_%d_%T_%s_%p.ctl';
  RELEASE CHANNEL ch1;
  RELEASE CHANNEL ch2;
}
EOF

# 检查备份结果
if [ $? -eq 0 ]; then
    log "备份成功完成"
    
    # 删除过期备份
    log "清理过期备份（保留 $RETENTION_DAYS 天）"
    rman target / <<EOF | tee -a $LOG_DIR/backup_${DATE}.log
    DELETE OBSOLETE;
    DELETE ARCHIVELOG ALL BACKED UP 1 TIMES TO DISK;
    EXIT;
EOF
    
    # 验证备份
    log "验证备份完整性"
    rman target / <<EOF | tee -a $LOG_DIR/backup_${DATE}.log
    VALIDATE BACKUPSET ALL;
    EXIT;
EOF
    
    log "========== 备份完成 =========="
    
    # 发送通知（可选）
    # echo "Oracle备份成功完成" | mail -s "备份成功" admin@example.com
    
    exit 0
else
    log "备份失败！"
    log "========== 备份失败 =========="
    
    # 发送告警（可选）
    # echo "Oracle备份失败，请检查日志" | mail -s "备份失败" admin@example.com
    
    exit 1
fi
```

#### 设置执行权限
```bash
chmod +x oracle_rman_backup.sh
```

#### 配置定时任务（crontab）
```bash
# 编辑 crontab
crontab -e

# 添加定时任务（每天凌晨2点执行全量备份）
0 2 * * * /backup/oracle/scripts/oracle_rman_backup.sh >> /backup/oracle/rman/logs/cron.log 2>&1

# 每周日凌晨3点执行全量备份，其他时间执行增量备份
0 3 * * 0 /backup/oracle/scripts/oracle_rman_backup.sh >> /backup/oracle/rman/logs/cron.log 2>&1
0 2 * * 1-6 /backup/oracle/scripts/oracle_rman_incremental.sh >> /backup/oracle/rman/logs/cron.log 2>&1
```

### 5.2 Data Pump 自动化备份脚本

```bash
#!/bin/bash
# Oracle Data Pump 自动备份脚本
# 文件名: oracle_datapump_backup.sh

# 配置变量
ORACLE_SID=ORCL
ORACLE_HOME=/u01/app/oracle/product/19.0.0/dbhome_1
DB_USER=system
DB_PASSWORD=password
DB_NAME=ORCL
BACKUP_DIR=/backup/oracle/datapump
LOG_DIR=/backup/oracle/datapump/logs
RETENTION_DAYS=7
DATE=$(date +%Y%m%d_%H%M%S)

# 设置环境变量
export ORACLE_SID
export ORACLE_HOME
export PATH=$ORACLE_HOME/bin:$PATH

# 创建目录
mkdir -p $BACKUP_DIR
mkdir -p $LOG_DIR

# 日志函数
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a $LOG_DIR/datapump_${DATE}.log
}

# 开始备份
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

# 检查备份结果
if [ $? -eq 0 ]; then
    log "备份成功完成"
    
    # 压缩备份文件（可选）
    log "压缩备份文件"
    gzip $BACKUP_DIR/*.dmp
    
    # 删除过期备份
    log "清理过期备份（保留 $RETENTION_DAYS 天）"
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

### 5.3 增量备份脚本

```bash
#!/bin/bash
# Oracle RMAN 增量备份脚本
# 文件名: oracle_rman_incremental.sh

ORACLE_SID=ORCL
ORACLE_HOME=/u01/app/oracle/product/19.0.0/dbhome_1
BACKUP_BASE=/backup/oracle/rman
LOG_DIR=/backup/oracle/rman/logs
DATE=$(date +%Y%m%d_%H%M%S)
DAY_OF_WEEK=$(date +%u)  # 1=Monday, 7=Sunday

export ORACLE_SID
export ORACLE_HOME
export PATH=$ORACLE_HOME/bin:$PATH

mkdir -p $LOG_DIR

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a $LOG_DIR/incremental_${DATE}.log
}

log "========== 开始增量备份 =========="

# 每周日执行0级增量（全量），其他时间执行1级增量
if [ $DAY_OF_WEEK -eq 7 ]; then
    log "执行0级增量备份（全量）"
    BACKUP_LEVEL=0
else
    log "执行1级增量备份"
    BACKUP_LEVEL=1
fi

rman target / <<EOF | tee -a $LOG_DIR/incremental_${DATE}.log
RUN {
  ALLOCATE CHANNEL ch1 TYPE DISK FORMAT '$BACKUP_BASE/level${BACKUP_LEVEL}_%d_%T_%s_%p.bkp';
  BACKUP INCREMENTAL LEVEL ${BACKUP_LEVEL} DATABASE PLUS ARCHIVELOG;
  RELEASE CHANNEL ch1;
}
DELETE OBSOLETE;
EXIT;
EOF

if [ $? -eq 0 ]; then
    log "增量备份成功完成"
    exit 0
else
    log "增量备份失败！"
    exit 1
fi
```

---

## 6. 常见问题处理

### 6.1 RMAN 备份问题

#### 问题1：备份空间不足
```bash
# 检查备份目录空间
df -h /backup/oracle

# 删除过期备份
rman target / <<EOF
DELETE OBSOLETE;
EXIT;
EOF

# 压缩备份
rman target / <<EOF
BACKUP AS COMPRESSED BACKUPSET DATABASE;
EXIT;
EOF
```

#### 问题2：归档日志空间不足
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
```

#### 问题3：备份失败
```bash
# 查看 RMAN 日志
tail -f $ORACLE_HOME/diag/rdbms/$ORACLE_SID/$ORACLE_SID/trace/alert_$ORACLE_SID.log

# 检查备份配置
rman target / <<EOF
SHOW ALL;
EXIT;
EOF
```

### 6.2 Data Pump 问题

#### 问题1：导出作业挂起
```sql
-- 查看作业状态
SELECT job_name, state, degree, attached_sessions 
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

#### 问题2：导入时表已存在
```bash
# 使用 TABLE_EXISTS_ACTION 参数
impdp system/password@database_name \
  SCHEMAS=user1 \
  DIRECTORY=dpump_dir \
  DUMPFILE=schema_backup.dmp \
  TABLE_EXISTS_ACTION=REPLACE  # 或 SKIP, APPEND, TRUNCATE
```

#### 问题3：字符集问题
```sql
-- 检查字符集
SELECT * FROM nls_database_parameters WHERE parameter LIKE '%CHARACTERSET%';

-- 导入时指定字符集
impdp system/password@database_name \
  DUMPFILE=backup.dmp \
  DIRECTORY=dpump_dir \
  NLS_LANG=AMERICAN_AMERICA.AL32UTF8
```

### 6.3 备份验证问题

#### 验证备份完整性
```bash
# RMAN 验证
rman target / <<EOF
VALIDATE BACKUPSET ALL;
RESTORE DATABASE VALIDATE;
EXIT;
EOF

# 检查备份文件大小
ls -lh /backup/oracle/rman/*.bkp

# 检查备份文件权限
ls -l /backup/oracle/rman/
```

---

## 7. 备份策略建议

### 7.1 生产环境备份策略

| 备份类型 | 频率 | 保留时间 | 说明 |
|---------|------|---------|------|
| 全量备份 | 每周1次 | 4周 | 周日执行 |
| 增量备份 | 每天1次 | 2周 | 周一至周六执行 |
| 归档日志 | 每小时 | 1周 | 实时备份 |
| Data Pump | 每周1次 | 2周 | 逻辑备份，用于迁移 |

### 7.2 备份检查清单

- [ ] 备份脚本已配置并测试
- [ ] 定时任务已设置
- [ ] 备份目录空间充足
- [ ] 备份文件权限正确
- [ ] 备份验证定期执行
- [ ] 恢复测试定期执行
- [ ] 备份日志定期检查
- [ ] 异地备份已配置
- [ ] 备份监控告警已配置

### 7.3 备份监控

```bash
# 检查最近备份
rman target / <<EOF
LIST BACKUP SUMMARY;
EXIT;
EOF

# 检查备份作业
sqlplus / as sysdba <<EOF
SELECT job_name, state, start_time, end_time 
FROM dba_datapump_jobs 
ORDER BY start_time DESC;
EXIT;
EOF

# 检查备份目录大小
du -sh /backup/oracle/*
```

---

## 8. 附录

### 8.1 常用 RMAN 命令参考

```bash
# 连接
rman target /                    # 本地连接
rman target sys/password@db     # 远程连接

# 备份
BACKUP DATABASE;                 # 全量备份
BACKUP INCREMENTAL LEVEL 0 DATABASE;  # 0级增量
BACKUP INCREMENTAL LEVEL 1 DATABASE;  # 1级增量
BACKUP TABLESPACE users;         # 表空间备份
BACKUP ARCHIVELOG ALL;           # 归档日志备份

# 恢复
RESTORE DATABASE;                # 恢复数据库
RECOVER DATABASE;                # 恢复数据库
RESTORE TABLESPACE users;        # 恢复表空间

# 查看
LIST BACKUP;                     # 列出备份
LIST BACKUP SUMMARY;             # 备份摘要
REPORT NEED BACKUP;              # 需要备份的报告
REPORT OBSOLETE;                 # 过期备份报告

# 删除
DELETE OBSOLETE;                 # 删除过期备份
DELETE BACKUPSET <key>;          # 删除指定备份集
```

### 8.2 常用 Data Pump 参数

```bash
# 导出参数
FULL=Y                           # 全库导出
SCHEMAS=user1,user2              # 用户导出
TABLES=table1,table2             # 表导出
DIRECTORY=dpump_dir              # 目录
DUMPFILE=backup.dmp              # 导出文件
LOGFILE=backup.log               # 日志文件
COMPRESSION=ALL                  # 压缩
PARALLEL=4                       # 并行度
FILESIZE=2G                      # 文件大小
EXCLUDE=TABLE:"IN ('T1','T2')"  # 排除对象

# 导入参数
FULL=Y                           # 全库导入
SCHEMAS=user1                    # 用户导入
TABLES=table1                    # 表导入
REMAP_SCHEMA=user1:user2         # 重映射用户
REMAP_TABLESPACE=tbs1:tbs2       # 重映射表空间
TABLE_EXISTS_ACTION=REPLACE      # 表存在处理
```

---

**文档版本**: v1.0  
**最后更新**: 2024-01-XX  
**适用版本**: Oracle 11g/12c/19c/21c

