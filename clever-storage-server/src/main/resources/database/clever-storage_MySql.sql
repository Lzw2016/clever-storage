create database if not exists `clever-storage` default character set = utf8;
use `clever-storage`;


/* ====================================================================================================================
    file_info -- 文件信息
==================================================================================================================== */
create table file_info
(
    id              bigint          not null    auto_increment                      comment '编号',
    stored_type     int(1)          not null                                        comment '上传文件的存储类型（1：当前服务器硬盘；2：ftp服务器；3：阿里云OSS）',
    stored_node     varchar(127)    not null                                        comment '文件存储节点',
    file_path       varchar(255)    not null                                        comment '上传文件存放路径',
    digest          varchar(255)    not null                                        comment '文件签名，用于判断是否是同一文件',
    digest_type     int(1)          not null                                        comment '文件签名算法类型（1：md5；2：sha-1；）',
    file_size       bigint          not null                                        comment '文件大小，单位：byte(1kb = 1024byte)',
    file_name       varchar(255)    not null                                        comment '文件原名称，用户上传时的名称',
    new_name        varchar(64)     not null    unique                              comment '文件当前名称（uuid + 后缀名）',
    file_suffix     varchar(15)                                                     comment '文件后缀名',
    public_read     int(1)          not null                                        comment '是否公开可以访问',
    public_write    int(1)          not null                                        comment '是否公开可以修改',
    read_url        varchar(255)                                                    comment '访问url',
    upload_time     bigint          not null                                        comment '文件上传所用时间（毫秒）',
    stored_time     bigint          not null                                        comment '文件存储用时，单位：毫秒（此时间只包含服务器端存储文件所用的时间，不包括文件上传所用的时间）',
    file_source     varchar(255)    not null                                        comment '文件来源（可以是系统模块名）',
    create_at       datetime(3)     not null    default current_timestamp(3)        comment '创建时间',
    update_at       datetime(3)                 on update current_timestamp(3)      comment '更新时间',
    primary key (id)
) comment = '上传文件信息表';
create index file_info_digest       on  file_info     (digest);
create index file_info_new_name     on  file_info     (new_name);
/*------------------------------------------------------------------------------------------------------------------------

--------------------------------------------------------------------------------------------------------------------------*/


/* ====================================================================================================================
    ftp_user -- ftp服务器用户
==================================================================================================================== */
create table ftp_user (
    id              bigint          not null    auto_increment                      comment '编号',
    userid          varchar(64)     not null                                        comment '用户id',
    password        varchar(64)                                                     comment '用户密码',
    homedirectory   varchar(128)    not null                                        comment '主目录',
    enableflag      boolean                     default true                        comment '当前用户可用',
    writepermission boolean                     default false                       comment '具有上传权限',
    idletime        int                         default 0                           comment '空闲时间',
    uploadrate      int                         default 0                           comment '上传速率限制（字节每秒）',
    downloadrate    int                         default 0                           comment '下载速率限制（字节每秒）',
    maxloginnumber  int                         default 0                           comment '最大登陆用户数',
    maxloginperip   int                         default 0                           comment '同ip登陆用户数',
    primary key (id)
) comment = 'ftp服务器用户';


/*------------------------------------------------------------------------------------------------------------------------
小文件(小于50MB)
    upload
        public_read
            true
                read_url(都可访问)
            false
                自己下载
        pulic_write
            true
            
            false

    upload_replace
        file_id
        
    modify
        
    download(public_read=false)
        
--------------------------------------------------------------------------------------------------------------------------*/


/* ====================================================================================================================
    ftp_user -- ftp服务器用户
==================================================================================================================== */
create table ftp_user (
    id              bigint          not null    auto_increment                      comment '编号',
    userid          varchar(64)     not null                                        comment '用户id',
    password        varchar(64)                                                     comment '用户密码',
    homedirectory   varchar(128)    not null                                        comment '主目录',
    enableflag      boolean                     default true                        comment '当前用户可用',
    writepermission boolean                     default false                       comment '具有上传权限',
    idletime        int                         default 0                           comment '空闲时间',
    uploadrate      int                         default 0                           comment '上传速率限制（字节每秒）',
    downloadrate    int                         default 0                           comment '下载速率限制（字节每秒）',
    maxloginnumber  int                         default 0                           comment '最大登陆用户数',
    maxloginperip   int                         default 0                           comment '同ip登陆用户数',
    primary key (id)
) comment = 'ftp服务器用户';
/*------------------------------------------------------------------------------------------------------------------------

--------------------------------------------------------------------------------------------------------------------------*/


















































