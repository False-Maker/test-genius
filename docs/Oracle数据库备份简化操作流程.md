# Oracle 数据库备份简化操作流程

## ⚠️ 重要提示

### 备份方式选择

- 【简单】离线备份：数据库关闭后备份，最简单，但需要停机（见"零、快速开始"章节）
- 【推荐】在线备份：数据库运行时备份，不需要停机，但需要启用归档模式（见"二、RMAN在线备份"章节）

### 通用注意事项

- 【重要】在线备份必须启用归档模式，否则备份期间的数据变化会丢失
- 【风险】切换到归档模式需要短暂停机，建议在维护窗口期执行
- 【重要】在线备份时一定要包含归档日志，否则无法完全恢复备份期间的数据变化
- 【风险】备份空间不足会导致备份失败，建议预留数据库大小的2-3倍空间
- 【重要】定期验证备份完整性，至少每季度做一次恢复测试

---

## 🔥 应急恢复：磁盘损坏场景（优先阅读）

### 场景说明

【紧急情况】数据库磁盘损坏，需要尽快导出数据，磁盘恢复后再导入

### 推荐方案：Data Pump 导出（逻辑备份）

【推荐理由】
- 导出的是数据内容，不依赖物理文件结构
- 即使部分磁盘损坏，只要能启动数据库就能导出
- 恢复时导入到新磁盘，不依赖原文件位置
- 可以压缩，节省空间

### 第一步：检查数据库状态

尝试启动数据库：
sqlplus / as sysdba
STARTUP;

如果启动成功，继续下一步。
如果启动失败，见"情况二：数据库无法启动"章节。

### 第二步：检查 Data Pump 是否可用

【重要】Data Pump 是 Oracle 数据库自带的工具，不需要单独安装！

检查命令是否存在：
which expdp
which impdp

如果找不到命令，检查 Oracle 环境变量：
echo $ORACLE_HOME
echo $PATH

如果环境变量未设置，需要先设置：
export ORACLE_HOME=/u01/app/oracle/product/19.0.0/dbhome_1  # 根据实际路径修改
export PATH=$ORACLE_HOME/bin:$PATH

或者直接使用完整路径：
$ORACLE_HOME/bin/expdp system/password@database_name ...

【说明】
- expdp：导出工具（Data Pump Export）
- impdp：导入工具（Data Pump Import）
- 这两个工具在 Oracle 10g 及以上版本都自带
- 如果数据库已安装，这两个工具一定存在

### 第三步：创建导出目录

如果目录不存在，先创建：
sqlplus / as sysdba
CREATE DIRECTORY dpump_dir AS '/backup/oracle/datapump';
CREATE DIRECTORY log_dir AS '/backup/oracle/datapump/logs';
GRANT READ, WRITE ON DIRECTORY dpump_dir TO system;
GRANT READ, WRITE ON DIRECTORY log_dir TO system;

确保操作系统目录存在：
mkdir -p /backup/oracle/datapump
mkdir -p /backup/oracle/datapump/logs
chown oracle:oinstall /backup/oracle/datapump -R

### 第四步：执行全库导出

【重要】导出到其他正常的磁盘，不要导出到损坏的磁盘

expdp system/password@database_name \
  FULL=Y \
  DIRECTORY=dpump_dir \
  DUMPFILE=emergency_backup_%U.dmp \
  LOGFILE=log_dir:emergency_backup.log \
  COMPRESSION=ALL \
  PARALLEL=4 \
  FILESIZE=2G

【说明】
- FULL=Y：导出整个数据库
- COMPRESSION=ALL：压缩导出，节省空间
- PARALLEL=4：并行导出，加快速度
- FILESIZE=2G：每个文件最大2GB，方便传输

### 第五步：检查导出结果

查看导出日志：
cat /backup/oracle/datapump/logs/emergency_backup.log

检查导出文件：
ls -lh /backup/oracle/datapump/*.dmp

【重要】确认导出成功后再进行磁盘修复

### 第六步：磁盘恢复后导入数据

磁盘修复后，在新磁盘上创建数据库（或使用现有数据库），然后导入：

步骤1：创建目录（如果不存在）
sqlplus / as sysdba
CREATE DIRECTORY dpump_dir AS '/backup/oracle/datapump';
CREATE DIRECTORY log_dir AS '/backup/oracle/datapump/logs';
GRANT READ, WRITE ON DIRECTORY dpump_dir TO system;
GRANT READ, WRITE ON DIRECTORY log_dir TO system;

步骤2：执行导入
impdp system/password@database_name \
  FULL=Y \
  DIRECTORY=dpump_dir \
  DUMPFILE=emergency_backup_%U.dmp \
  LOGFILE=log_dir:emergency_import.log \
  PARALLEL=4

【风险】如果目标数据库已有数据，导入会覆盖，请谨慎操作

### 情况二：数据库无法启动

如果数据库无法启动，尝试以下方法：

#### 方法1：尝试启动到mount状态

sqlplus / as sysdba
STARTUP MOUNT;

如果mount成功，可以尝试：
- 使用RMAN恢复部分数据
- 或者尝试打开数据库（可能丢失部分数据）

#### 方法2：检查损坏的文件

sqlplus / as sysdba
STARTUP MOUNT;
SELECT file#, name, status FROM v$datafile WHERE status != 'ONLINE';

如果只是部分文件损坏，可以：
1. 将损坏的文件离线
2. 打开数据库
3. 导出未损坏的数据
4. 尝试恢复损坏文件的数据

#### 方法3：使用RMAN恢复

如果之前有RMAN备份：
rman target /
STARTUP MOUNT;
RESTORE DATABASE;
RECOVER DATABASE;
ALTER DATABASE OPEN;

### 按用户导出（如果全库导出失败）

如果全库导出失败，可以尝试按用户导出：

expdp system/password@database_name \
  SCHEMAS=user1,user2,user3 \
  DIRECTORY=dpump_dir \
  DUMPFILE=schema_backup_%U.dmp \
  LOGFILE=log_dir:schema_backup.log \
  COMPRESSION=ALL

查看所有用户：
sqlplus / as sysdba
SELECT username FROM dba_users WHERE username NOT IN ('SYS', 'SYSTEM', 'SYSAUX');

### 按表导出（如果用户导出也失败）

如果用户导出也失败，可以尝试导出关键表：

expdp system/password@database_name \
  TABLES=user1.table1,user1.table2 \
  DIRECTORY=dpump_dir \
  DUMPFILE=table_backup.dmp \
  LOGFILE=log_dir:table_backup.log

### 应急恢复检查清单

- [ ] 数据库是否能启动？
- [ ] 导出目录是否在正常磁盘上？
- [ ] 导出空间是否充足？
- [ ] 导出是否成功完成？
- [ ] 导出文件是否已备份到安全位置？
- [ ] 磁盘修复计划是否已制定？
- [ ] 导入测试环境是否已准备？

### 重要提示

【风险】磁盘损坏时，优先保证数据导出，不要急于修复磁盘
【重要】导出文件要保存到多个位置，防止再次丢失
【建议】导出完成后，立即将文件复制到其他服务器或云存储
【重要】磁盘恢复后，先在测试环境验证导入，确认无误后再在生产环境操作

---

## 零、快速开始：离线备份（最简单）

### 适用场景

【优势】离线备份最简单，适合以下场景：
- 可以接受短暂停机（维护窗口期）
- 只需要备份某个时间点的数据快照
- 不需要考虑备份期间的数据变化
- 测试环境或非关键业务系统

【缺点】需要停机，备份期间业务不可用

### 方法一：RMAN 冷备份（推荐）

这是最简单的备份方式，数据库关闭后直接备份：

步骤1：关闭数据库
sqlplus / as sysdba
SHUTDOWN IMMEDIATE;

步骤2：启动到mount状态（不打开数据库）
STARTUP MOUNT;

步骤3：执行备份（数据库处于关闭状态）
rman target /
BACKUP DATABASE FORMAT '/backup/oracle/cold_backup_%d_%T_%s_%p.bkp';
BACKUP CURRENT CONTROLFILE FORMAT '/backup/oracle/cold_control_%F';

步骤4：打开数据库
ALTER DATABASE OPEN;

【说明】离线备份不需要考虑归档日志，因为数据库关闭时没有数据变化

### 方法二：操作系统文件复制（最简单）

直接复制数据库文件，最简单粗暴：

步骤1：关闭数据库
sqlplus / as sysdba
SHUTDOWN IMMEDIATE;

步骤2：查找所有数据库文件位置
-- 查看数据文件
SELECT file_name FROM dba_data_files;

-- 查看控制文件
SELECT name FROM v$controlfile;

-- 查看日志文件
SELECT member FROM v$logfile;

步骤3：复制所有文件到备份目录
mkdir -p /backup/oracle/cold_backup_$(date +%Y%m%d)
cp /u01/app/oracle/oradata/ORCL/datafile/*.dbf /backup/oracle/cold_backup_$(date +%Y%m%d)/
cp /u01/app/oracle/oradata/ORCL/controlfile/*.ctl /backup/oracle/cold_backup_$(date +%Y%m%d)/
cp /u01/app/oracle/oradata/ORCL/onlinelog/*.log /backup/oracle/cold_backup_$(date +%Y%m%d)/

步骤4：打开数据库
sqlplus / as sysdba
STARTUP;

【风险】必须确保所有文件都复制了，缺少任何一个文件都无法恢复

### 方法三：Data Pump 导出（逻辑备份）

如果只需要导出数据，不关心物理文件：

步骤1：数据库可以运行（不需要关闭）
expdp system/password@database_name \
  FULL=Y \
  DIRECTORY=dpump_dir \
  DUMPFILE=full_backup_%U.dmp \
  LOGFILE=log_dir:full_backup.log \
  COMPRESSION=ALL

【说明】这是逻辑备份，导出的是数据内容，不是物理文件

### 离线备份恢复

RMAN冷备份恢复：
rman target /
SHUTDOWN IMMEDIATE;
STARTUP MOUNT;
RESTORE DATABASE;
ALTER DATABASE OPEN;

操作系统文件恢复：
sqlplus / as sysdba
SHUTDOWN IMMEDIATE;
-- 将备份的文件复制回原位置
cp /backup/oracle/cold_backup_20240115/* /u01/app/oracle/oradata/ORCL/
STARTUP;

【重要】离线备份只能恢复到备份时间点，无法恢复到备份之后的时间点

---

## 一、备份前准备

### 1.1 检查数据库状态

执行以下SQL检查数据库是否正常运行：

sqlplus / as sysdba
SELECT status FROM v$instance;  -- 应该显示 OPEN
SELECT * FROM v$version;         -- 查看版本
SELECT name FROM v$database;     -- 查看数据库名

【重要】数据库状态必须是 OPEN 才能进行在线备份

### 1.2 检查归档模式（关键！）

执行以下SQL检查归档模式：

sqlplus / as sysdba
SELECT log_mode FROM v$database;

- 如果显示 ARCHIVELOG：可以继续，直接跳到1.3节
- 如果显示 NOARCHIVELOG：必须切换到归档模式

【风险】切换到归档模式需要短暂停机，请在维护窗口期执行
【重要】归档模式只需要设置一次，之后可以一直使用在线备份

#### 切换到归档模式的步骤：

sqlplus / as sysdba
SHUTDOWN IMMEDIATE;              -- 关闭数据库
STARTUP MOUNT;                   -- 启动到mount状态
ALTER DATABASE ARCHIVELOG;       -- 启用归档模式
ALTER DATABASE OPEN;             -- 打开数据库
SELECT log_mode FROM v$database; -- 验证（应该显示 ARCHIVELOG）

-- 配置归档日志存储位置（可选但推荐）
ALTER SYSTEM SET log_archive_dest_1='LOCATION=/backup/oracle/archive' SCOPE=SPFILE;
ALTER SYSTEM SET log_archive_format='arch_%t_%s_%r.log' SCOPE=SPFILE;

### 1.3 创建备份目录

在操作系统层面创建备份目录：

mkdir -p /backup/oracle/rman
mkdir -p /backup/oracle/archive
mkdir -p /backup/oracle/rman/logs
mkdir -p /backup/oracle/datapump
mkdir -p /backup/oracle/datapump/logs
chown oracle:oinstall /backup/oracle -R
chmod 755 /backup/oracle -R

在数据库中创建目录对象：

sqlplus / as sysdba
CREATE DIRECTORY backup_dir AS '/backup/oracle/rman';
CREATE DIRECTORY archive_dir AS '/backup/oracle/archive';
CREATE DIRECTORY dpump_dir AS '/backup/oracle/datapump';
CREATE DIRECTORY log_dir AS '/backup/oracle/datapump/logs';

GRANT READ, WRITE ON DIRECTORY backup_dir TO system;
GRANT READ, WRITE ON DIRECTORY archive_dir TO system;
GRANT READ, WRITE ON DIRECTORY dpump_dir TO system;
GRANT READ, WRITE ON DIRECTORY log_dir TO system;

SELECT * FROM dba_directories WHERE directory_name IN ('BACKUP_DIR', 'ARCHIVE_DIR', 'DPUMP_DIR', 'LOG_DIR');

### 1.4 检查磁盘空间

【风险】备份空间不足会导致备份失败

检查备份目录可用空间：
df -h /backup/oracle

检查数据库大小：
sqlplus / as sysdba <<EOF
SELECT ROUND(SUM(bytes)/1024/1024/1024, 2) AS "Database Size (GB)" FROM dba_data_files;
EXIT;
EOF

【重要】建议备份目录可用空间至少是数据库大小的2-3倍

---

## 二、RMAN 在线备份流程

### 2.1 配置 RMAN

【重要】RMAN配置只需要做一次，之后一直有效

rman target /
SHOW ALL;  -- 查看当前配置

-- 配置备份保留策略（保留7天）
CONFIGURE RETENTION POLICY TO REDUNDANCY 7;

-- 配置备份设备类型
CONFIGURE DEFAULT DEVICE TYPE TO DISK;

-- 配置备份格式
CONFIGURE CHANNEL DEVICE TYPE DISK FORMAT '/backup/oracle/rman/%d_%T_%s_%p.bkp';

-- 【重要】配置控制文件自动备份
CONFIGURE CONTROLFILE AUTOBACKUP ON;
CONFIGURE CONTROLFILE AUTOBACKUP FORMAT FOR DEVICE TYPE DISK TO '/backup/oracle/rman/control_%F';

-- 配置归档日志删除策略（已备份的自动删除）
CONFIGURE ARCHIVELOG DELETION POLICY TO BACKED UP 1 TIMES TO DISK;

SHOW ALL;  -- 再次查看确认配置

### 2.2 完整在线备份流程（推荐）

【重要】这是最完整的备份流程，确保备份期间所有数据变化都被捕获

备份原理说明：
- 数据库在备份期间保持OPEN状态，业务正常运行
- 备份开始前先备份已有的归档日志（备份开始前的数据变化）
- 备份数据库文件时，新的数据变化会写入重做日志（Redo Log）
- 备份完成后切换日志，将重做日志转为归档日志（包含备份期间的所有数据变化）
- 备份这些归档日志，这样恢复时就能恢复到备份完成时的完整状态

备份命令：

rman target / <<EOF
RUN {
  -- 第一步：备份备份开始前已存在的所有归档日志
  BACKUP ARCHIVELOG ALL DELETE INPUT;
  
  -- 第二步：备份数据库（在线备份，数据库保持OPEN）
  ALLOCATE CHANNEL ch1 TYPE DISK FORMAT '/backup/oracle/rman/full_%d_%T_%s_%p.bkp';
  ALLOCATE CHANNEL ch2 TYPE DISK FORMAT '/backup/oracle/rman/full_%d_%T_%s_%p.bkp';
  
  BACKUP AS COMPRESSED BACKUPSET DATABASE TAG 'FULL_BACKUP';
  
  -- 第三步：强制切换日志，将备份期间的数据变化归档
  SQL "ALTER SYSTEM ARCHIVE LOG CURRENT";
  
  -- 第四步：备份备份期间产生的归档日志（包含备份期间的所有数据变化）
  BACKUP ARCHIVELOG ALL DELETE INPUT;
  
  -- 第五步：备份控制文件和SPFILE
  BACKUP CURRENT CONTROLFILE;
  BACKUP SPFILE;
  
  RELEASE CHANNEL ch1;
  RELEASE CHANNEL ch2;
}
EOF

【重要】为什么必须切换日志？
- 如果不切换日志，备份期间的数据变化只记录在重做日志中，还没有归档
- 切换日志后，重做日志转为归档日志，才能被备份
- 恢复时，需要这些归档日志才能完全恢复备份期间的数据变化

### 2.3 增量备份

增量备份策略：
- 0级增量：相当于全量备份，备份所有数据块
- 1级增量：只备份自上次0级或1级备份后变化的数据块

#### 0级增量备份（全量）

rman target / <<EOF
RUN {
  BACKUP ARCHIVELOG ALL DELETE INPUT;
  
  ALLOCATE CHANNEL ch1 TYPE DISK FORMAT '/backup/oracle/rman/level0_%d_%T_%s_%p.bkp';
  BACKUP INCREMENTAL LEVEL 0 AS COMPRESSED BACKUPSET DATABASE TAG 'LEVEL0_BACKUP';
  
  SQL "ALTER SYSTEM ARCHIVELOG CURRENT";
  BACKUP ARCHIVELOG ALL DELETE INPUT;
  BACKUP CURRENT CONTROLFILE;
  
  RELEASE CHANNEL ch1;
}
EOF

#### 1级增量备份

rman target / <<EOF
RUN {
  BACKUP ARCHIVELOG ALL DELETE INPUT;
  
  ALLOCATE CHANNEL ch1 TYPE DISK FORMAT '/backup/oracle/rman/level1_%d_%T_%s_%p.bkp';
  BACKUP INCREMENTAL LEVEL 1 AS COMPRESSED BACKUPSET DATABASE TAG 'LEVEL1_BACKUP';
  
  SQL "ALTER SYSTEM ARCHIVELOG CURRENT";
  BACKUP ARCHIVELOG ALL DELETE INPUT;
  BACKUP CURRENT CONTROLFILE;
  
  RELEASE CHANNEL ch1;
}
EOF

【提示】增量备份比全量备份快，但恢复时需要先恢复0级备份，再依次应用1级备份

### 2.4 查看备份信息

rman target /
LIST BACKUP;                    -- 查看所有备份
LIST BACKUP SUMMARY;            -- 查看备份摘要
LIST BACKUP OF DATABASE;        -- 查看数据库备份
LIST BACKUP OF ARCHIVELOG ALL;  -- 查看归档日志备份
LIST BACKUP OF CONTROLFILE;     -- 查看控制文件备份
REPORT NEED BACKUP;             -- 查看需要备份的报告
REPORT OBSOLETE;                -- 查看过期备份

### 2.5 删除过期备份

【风险】删除备份不可恢复，请确认后再执行

rman target / <<EOF
DELETE OBSOLETE;  -- 根据保留策略删除过期备份
DELETE ARCHIVELOG ALL BACKED UP 1 TIMES TO DISK;  -- 删除已备份的归档日志
EXIT;
EOF

---

## 三、备份期间数据处理机制

### 3.1 为什么在线备份不会丢失数据？

简单理解：
1. 备份开始时：备份已有的归档日志（备份前的数据变化）
2. 备份数据库文件时：数据库继续运行，新数据变化写入重做日志
3. 备份完成后：切换日志，重做日志转为归档日志
4. 备份归档日志：备份包含备份期间数据变化的归档日志
5. 恢复时：恢复数据文件 + 应用归档日志 = 完整状态

时间线示例：

10:00:00 - 开始备份，备份已有归档日志
10:00:30 - 开始备份数据文件1、2
10:01:00 - 用户插入100条记录 → 写入数据文件，记录到重做日志
10:02:00 - 继续备份数据文件3
10:03:00 - 数据库备份完成
10:03:10 - 切换日志（ALTER SYSTEM ARCHIVE LOG CURRENT）
           └─ 生成归档日志（包含10:01:00插入的100条记录）
10:03:20 - 备份归档日志
10:03:30 - 备份控制文件
10:03:40 - 备份完成

恢复时：
- 恢复数据文件（恢复到10:00:30-10:02:00的状态）
- 应用归档日志（包含10:01:00插入的100条记录）
- 结果：数据库恢复到10:03:40的完整状态 ✓

### 3.2 错误做法 vs 正确做法

❌ 错误做法（会丢失数据）：
BACKUP DATABASE;  -- 只备份数据库，不备份归档日志
-- 问题：备份期间的数据变化会丢失！

✅ 正确做法：
-- 方法1：简单方式
BACKUP DATABASE PLUS ARCHIVELOG;

-- 方法2：完整流程（推荐）
RUN {
  BACKUP ARCHIVELOG ALL;
  BACKUP DATABASE;
  SQL "ALTER SYSTEM ARCHIVELOG CURRENT";
  BACKUP ARCHIVELOG ALL;
}

### 3.3 归档日志管理

查看归档日志信息：
sqlplus / as sysdba
SELECT sequence#, name, completion_time FROM v$archived_log ORDER BY sequence# DESC;
SELECT name, value FROM v$parameter WHERE name LIKE 'log_archive_dest%';

【风险】归档日志占用大量空间，需要定期清理

检查归档日志空间：
du -sh /backup/oracle/archive/*

删除已备份的归档日志：
rman target / <<EOF
DELETE ARCHIVELOG ALL BACKED UP 1 TIMES TO DISK;
EXIT;
EOF

---

## 四、Data Pump 备份（逻辑备份）

【说明】Data Pump 是逻辑备份工具，主要用于数据迁移，不适合作为主要的备份手段

### 4.1 全库导出

expdp system/password@database_name \
  FULL=Y \
  DIRECTORY=dpump_dir \
  DUMPFILE=full_backup_%U.dmp \
  LOGFILE=log_dir:full_backup.log \
  COMPRESSION=ALL \
  PARALLEL=4 \
  FILESIZE=2G

### 4.2 按用户导出

expdp system/password@database_name \
  SCHEMAS=user1,user2 \
  DIRECTORY=dpump_dir \
  DUMPFILE=schema_backup_%U.dmp \
  LOGFILE=log_dir:schema_backup.log \
  COMPRESSION=ALL

### 4.3 查看导出进度

sqlplus / as sysdba
SELECT job_name, state, ROUND(bytes_processed/total_bytes*100, 2) AS progress_pct
FROM dba_datapump_jobs
WHERE state = 'EXECUTING';

### 4.4 导入操作

全库导入：
impdp system/password@database_name \
  FULL=Y \
  DIRECTORY=dpump_dir \
  DUMPFILE=full_backup_%U.dmp \
  LOGFILE=log_dir:full_import.log \
  PARALLEL=4

按用户导入：
impdp system/password@database_name \
  SCHEMAS=user1 \
  DIRECTORY=dpump_dir \
  DUMPFILE=schema_backup.dmp \
  LOGFILE=log_dir:schema_import.log \
  TABLE_EXISTS_ACTION=REPLACE

---

## 五、备份验证与恢复

### 5.1 验证备份完整性

【重要】定期验证备份，确保可以恢复

rman target / <<EOF
VALIDATE BACKUPSET ALL;        -- 验证所有备份集
RESTORE DATABASE VALIDATE;     -- 验证数据库文件（不实际恢复）
RESTORE ARCHIVELOG ALL VALIDATE;  -- 验证归档日志
EXIT;
EOF

### 5.2 恢复整个数据库

【风险】恢复操作会覆盖现有数据，请确认后再执行

rman target / <<EOF
SHUTDOWN IMMEDIATE;
STARTUP MOUNT;
RESTORE DATABASE;      -- 恢复数据文件
RECOVER DATABASE;      -- 应用归档日志（恢复备份期间的数据变化）
ALTER DATABASE OPEN;   -- 打开数据库
EXIT;
EOF

### 5.3 时间点恢复（PITR）

恢复到指定时间点：
rman target / <<EOF
RUN {
  SHUTDOWN IMMEDIATE;
  STARTUP MOUNT;
  SET UNTIL TIME "TO_DATE('2024-01-15 14:30:00', 'YYYY-MM-DD HH24:MI:SS')";
  RESTORE DATABASE;
  RECOVER DATABASE;
  ALTER DATABASE OPEN RESETLOGS;
}
EOF

### 5.4 恢复表空间

rman target / <<EOF
SQL "ALTER TABLESPACE users OFFLINE IMMEDIATE";
RESTORE TABLESPACE users;
RECOVER TABLESPACE users;
SQL "ALTER TABLESPACE users ONLINE";
EXIT;
EOF

---

## 六、自动化备份脚本

### 6.1 完整RMAN自动备份脚本

创建脚本文件：/backup/oracle/scripts/oracle_rman_backup.sh

#!/bin/bash
# Oracle RMAN 自动备份脚本

ORACLE_SID=ORCL
ORACLE_HOME=/u01/app/oracle/product/19.0.0/dbhome_1
BACKUP_BASE=/backup/oracle/rman
LOG_DIR=/backup/oracle/rman/logs
RETENTION_DAYS=7
DATE=$(date +%Y%m%d_%H%M%S)
DAY_OF_WEEK=$(date +%u)  # 1=Monday, 7=Sunday

export ORACLE_SID
export ORACLE_HOME
export PATH=$ORACLE_HOME/bin:$PATH

mkdir -p $LOG_DIR

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a $LOG_DIR/backup_${DATE}.log
}

log "========== 开始 RMAN 备份 =========="
log "备份时间: $(date)"
log "数据库: $ORACLE_SID"

# 检查数据库状态
DB_STATUS=$(sqlplus -s / as sysdba <<EOF
SET PAGESIZE 0 FEEDBACK OFF VERIFY OFF HEADING OFF ECHO OFF
SELECT status FROM v\$instance;
EXIT;
EOF
)

if [ "$DB_STATUS" != "OPEN" ]; then
    log "错误: 数据库状态不是OPEN，当前状态: $DB_STATUS"
    exit 1
fi

# 检查归档模式
ARCH_MODE=$(sqlplus -s / as sysdba <<EOF
SET PAGESIZE 0 FEEDBACK OFF VERIFY OFF HEADING OFF ECHO OFF
SELECT log_mode FROM v\$database;
EXIT;
EOF
)

if [ "$ARCH_MODE" != "ARCHIVELOG" ]; then
    log "错误: 数据库不是归档模式，当前模式: $ARCH_MODE"
    exit 1
fi

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

# 执行备份
rman target / <<EOF | tee -a $LOG_DIR/backup_${DATE}.log
RUN {
  BACKUP ARCHIVELOG ALL DELETE INPUT;
  
  ALLOCATE CHANNEL ch1 TYPE DISK FORMAT '$BACKUP_BASE/${BACKUP_TAG}_%d_%T_%s_%p.bkp';
  ALLOCATE CHANNEL ch2 TYPE DISK FORMAT '$BACKUP_BASE/${BACKUP_TAG}_%d_%T_%s_%p.bkp';
  
  BACKUP INCREMENTAL LEVEL ${BACKUP_LEVEL}
    AS COMPRESSED BACKUPSET 
    DATABASE 
    TAG '${BACKUP_TAG}';
  
  SQL "ALTER SYSTEM ARCHIVE LOG CURRENT";
  BACKUP ARCHIVELOG ALL DELETE INPUT;
  BACKUP CURRENT CONTROLFILE;
  BACKUP SPFILE;
  
  RELEASE CHANNEL ch1;
  RELEASE CHANNEL ch2;
}
EXIT;
EOF

if [ $? -eq 0 ]; then
    log "备份成功完成"
    
    # 验证备份
    rman target / <<EOF | tee -a $LOG_DIR/backup_${DATE}.log
    VALIDATE BACKUPSET ALL;
    EXIT;
EOF
    
    # 删除过期备份
    rman target / <<EOF | tee -a $LOG_DIR/backup_${DATE}.log
    DELETE OBSOLETE;
    DELETE ARCHIVELOG ALL BACKED UP 1 TIMES TO DISK;
    EXIT;
EOF
    
    log "========== 备份完成 =========="
    exit 0
else
    log "备份失败！"
    exit 1
fi

设置执行权限：
chmod +x /backup/oracle/scripts/oracle_rman_backup.sh

### 6.2 配置定时任务

编辑 crontab：
crontab -e

添加以下行：
# 每周日凌晨3点执行0级增量备份（全量）
0 3 * * 0 /backup/oracle/scripts/oracle_rman_backup.sh >> /backup/oracle/rman/logs/cron.log 2>&1

# 周一到周六凌晨2点执行1级增量备份
0 2 * * 1-6 /backup/oracle/scripts/oracle_rman_backup.sh >> /backup/oracle/rman/logs/cron.log 2>&1

---

## 七、常见问题处理

### 7.1 备份空间不足

【风险】备份空间不足会导致备份失败

检查空间：
df -h /backup/oracle

解决方法：
1. 删除过期备份：
rman target / <<EOF
DELETE OBSOLETE;
EXIT;
EOF

2. 使用压缩备份（节省空间）：
BACKUP AS COMPRESSED BACKUPSET DATABASE;

### 7.2 归档日志空间不足

【风险】归档日志空间满了会导致数据库挂起

检查归档日志：
rman target / <<EOF
LIST ARCHIVELOG ALL;
EXIT;
EOF

删除已备份的归档日志：
rman target / <<EOF
DELETE ARCHIVELOG ALL BACKED UP 1 TIMES TO DISK;
EXIT;
EOF

配置自动删除策略：
rman target / <<EOF
CONFIGURE ARCHIVELOG DELETION POLICY TO BACKED UP 1 TIMES TO DISK;
EXIT;
EOF

### 7.3 备份失败

查看日志：
tail -f $ORACLE_HOME/diag/rdbms/$ORACLE_SID/$ORACLE_SID/trace/alert_$ORACLE_SID.log

检查配置和状态：
rman target / <<EOF
SHOW ALL;
EXIT;
EOF

sqlplus / as sysdba <<EOF
SELECT status FROM v\$instance;
SELECT log_mode FROM v\$database;
EXIT;
EOF

### 7.4 Data Pump 作业挂起

查看作业状态：
sqlplus / as sysdba
SELECT job_name, state, start_time, last_update_time FROM dba_datapump_jobs;

停止作业：
BEGIN
  DBMS_DATAPUMP.STOP_JOB('JOB_NAME', 'IMMEDIATE');
END;
/

### 7.5 恢复时找不到归档日志

【风险】缺少归档日志会导致恢复失败

注册归档日志：
rman target / <<EOF
CATALOG ARCHIVELOG '/path/to/archive_log.log';
EXIT;
EOF

或指定归档日志目录：
rman target / <<EOF
SET ARCHIVELOG DESTINATION TO '/backup/oracle/archive';
RECOVER DATABASE;
EXIT;
EOF

---

## 八、备份策略建议

### 8.1 生产环境备份策略

| 备份类型 | 频率 | 保留时间 | 执行时间 | 说明 |
|---------|------|---------|---------|------|
| 0级增量（全量） | 每周1次 | 4周 | 周日凌晨3点 | 完整备份 |
| 1级增量 | 每天1次 | 2周 | 周一至周六凌晨2点 | 增量备份 |
| 归档日志 | 实时 | 1周 | 自动 | 捕获所有数据变化 |
| Data Pump | 每周1次 | 2周 | 周日晚上 | 逻辑备份，用于迁移 |

### 8.2 备份检查清单

- [ ] 数据库处于归档模式（ARCHIVELOG）
- [ ] 备份脚本已配置并测试
- [ ] 定时任务已设置
- [ ] 备份目录空间充足（至少数据库大小的2-3倍）
- [ ] 备份文件权限正确
- [ ] 备份验证定期执行（每周）
- [ ] 恢复测试定期执行（至少每季度一次）
- [ ] 备份日志定期检查
- [ ] 异地备份已配置
- [ ] 备份监控告警已配置

### 8.3 备份监控命令

检查最近备份：
rman target / <<EOF
LIST BACKUP SUMMARY;
EXIT;
EOF

检查备份目录大小：
du -sh /backup/oracle/*

检查备份文件数量：
find /backup/oracle/rman -name "*.bkp" | wc -l

---

## 九、关键要点总结

### 9.1 在线备份要点

1. 【重要】必须启用归档模式：在线备份的前提条件
2. 【重要】必须备份归档日志：使用 PLUS ARCHIVELOG 或完整流程
3. 【重要】备份期间数据不丢失：通过归档日志机制保证
4. 【优势】无需停机：数据库保持OPEN状态，业务正常运行

### 9.2 完整备份流程要点

完整备份流程（确保数据不丢失）：

1. 备份当前归档日志（备份开始前的数据变化）
2. 备份数据库文件（在线备份）
3. 切换日志（ALTER SYSTEM ARCHIVE LOG CURRENT）
4. 备份备份期间产生的归档日志（备份期间的数据变化）
5. 备份控制文件和SPFILE

### 9.3 恢复要点

恢复流程：

1. 恢复数据文件（RESTORE DATABASE）
2. 应用归档日志（RECOVER DATABASE）
   └─ 包含备份期间的所有数据变化
3. 打开数据库（ALTER DATABASE OPEN）

---

**文档版本**: v3.0  
**最后更新**: 2024-01-XX  
**适用版本**: Oracle 11g/12c/19c/21c  
**关键特性**: 在线备份、数据不丢失、完整流程、通俗易懂

