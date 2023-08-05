drop table if exists system_oper;
create table system_oper(  
  id int(11) not null auto_increment comment '主键',
  oper_type tinyint(2) default 0,
  oper_name varchar(50) not null,
  oper_code varchar(50) not null,
  module_code varchar(50) not null,
  parent_id int default 0,
  primary key (id),
  UNIQUE KEY oper_code_idx (oper_code)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists tenant_org;
create table tenant_org(  
  id int(11) not null auto_increment comment '主键',
  name varchar(100) not null,
  logo varchar(255), 
  domain varchar(255), 
  mode tinyint(2) default 1,
  full_name varchar(100),
  corporate varchar(100), 
  license varchar(100), 
  industry tinyint(2) default 0, 
  mobile varchar(20), 
  address varchar(100), 
  poster_url varchar(255), 
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists tenant_pay_order;
create table tenant_pay_order(  
  id int(11) not null auto_increment comment '主键',
  order_type tinyint(2) not null,
  ref_order_id int default 0,
  order_code varchar(20) not null,
  pay_order_id varchar(100), 
  error_msg varchar(250), 
  real_name varchar(50), 
  order_name varchar(255) not null,
  amount decimal(10,2) not null,
  order_time datetime not null,
  thirdpay_code varchar(50), 
  thirdpay_name varchar(50), 
  pay_status tinyint(2) default 1,
  order_desc varchar(255) default '',
  attach varchar(500), 
  prepay_id varchar(1024) default '',
  remark varchar(100), 
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists tenant_pay_order_log;
create table tenant_pay_order_log(  
  id int(11) not null auto_increment comment '主键',
  order_type tinyint(2) not null,
  ref_order_id int default 0,
  order_code varchar(20) not null,
  pay_order_id varchar(100), 
  error_msg varchar(250), 
  real_name varchar(50), 
  order_name varchar(255) not null,
  amount decimal(10,2) not null,
  order_time datetime not null,
  thirdpay_code varchar(50), 
  thirdpay_name varchar(50), 
  pay_status tinyint(2) default 1,
  order_desc varchar(255) default '',
  attach varchar(500), 
  prepay_id varchar(1024) default '',
  remark varchar(100), 
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists tenant_config;
create table tenant_config(  
  id int(11) not null auto_increment comment '主键',
  attr_key varchar(50) not null, 
  attr_value varchar(888) not null, 
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists tenant_admin;
create table tenant_admin(  
  id int(11) not null auto_increment comment '主键',
  mobile varchar(20) not null, 
  real_name varchar(100) not null,
  code varchar(20), 
  password varchar(100),
  encrypt varchar(100),
  role_id int not null,
  active_status tinyint(2) default 0,
  login_flag tinyint(1) default 1,
  main_account_flag tinyint(1) default 0,
  first_login_ip varchar(50), 
  first_login_time datetime, 
  last_login_ip varchar(50), 
  last_login_time datetime, 
  gender tinyint(2) default 2,
  login_error tinyint(2) default 0,
  login_count int default 0,
  mac varchar(200) default '',
  online_times int default 0,
  remark varchar(255), 
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists tenant_role;
create table tenant_role(  
  id int(11) not null auto_increment comment '主键',
   
  name varchar(50) not null,
  remark varchar(255), 
  super_flag tinyint(1) default 0,
  default_flag tinyint(1) default 0,
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists tenant_role_oper_ref;
create table tenant_role_oper_ref(  
  id int(11) not null auto_increment comment '主键',
   
  role_id int not null, 
  oper_code varchar(50) not null,
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists tenant_sms_log;
create table tenant_sms_log(  
  id int(11) not null auto_increment comment '主键',
   
  content text not null, 
  user_info_id int not null, 
  mobile varchar(20) not null, 
  send_flag tinyint(1) default 0, 
  retry_count tinyint(2) default 0, 
  send_desc varchar(255),  
  created_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists tenant_oper_log;
create table tenant_oper_log(  
  id int(11) not null auto_increment comment '主键',
  tenant_admin_id int not null,
  oper_person varchar(255) not null,
  oper_time datetime not null,
  oper_ip varchar(20), 
  oper_desc varchar(1024) not null,
  log_type tinyint(2) not null,
  platform_type tinyint(2) not null, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists tenant_print_template;
create table tenant_print_template(  
  id int(11) not null auto_increment comment '主键',
  
  name varchar(100) not null, 
  paper_type varchar(50) default '',
  print_mode tinyint(2) default 0,
  order_type tinyint(2) default 1,
  content text not null,
  tpl_pic varchar(255), 
  tpl_css text, 
  default_flag tinyint(1) default 0,
  remark  varchar(100), 
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;



drop table if exists tenant_export_log;
create table tenant_export_log(  
  id int(11) not null auto_increment comment '主键',
   
  handler_id int not null,
  file_name varchar(50) not null, 
  file_path varchar(255) default '', 
  export_status tinyint(2) default 1, 
  error_desc varchar(255) default '',
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;



drop table if exists msg_notice;
create table msg_notice(
  id int(11) not null auto_increment comment '主键',
   
  msg_level tinyint(2) default 0,
  msg_type tinyint(2) default 0,
  sender_id int(11) not null,
  sender_name varchar(50), 
  title varchar(255), 
  content text, 
  data_type tinyint(2) default 0,
  created_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;



drop table if exists msg_notice_send;
create table msg_notice_send(  
  id int(11) not null auto_increment comment '主键',
   
  msg_notice_id int not null,
  sender_id int(11) not null,
  sender_name varchar(50), 
  receiver_id int, 
  read_flag tinyint(1) default 0,
  valid_time datetime, 
  read_time datetime, 
  created_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists goods_spec;
create table goods_spec(  
  id int(11) not null auto_increment comment '主键',
   
  name varchar(100) not null, 
  data_status tinyint(2) default 1,
  created_at datetime, 
  updated_at datetime,
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists goods_spec_options;
create table goods_spec_options(  
  id int(11) not null auto_increment comment '主键',
  goods_spec_id int not null, 
  option_value varchar(100) not null,  
  data_status tinyint(2) default 1,
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists goods_unit;
create table goods_unit(  
  id int(11) not null auto_increment comment '主键',
  name varchar(100) not null, 
  data_status tinyint(2) default 1,
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists goods_attribute;
create table goods_attribute(  
  id int(11) not null auto_increment comment '主键',
  name varchar(100) not null, 
  attr_type tinyint(2) default 2, 
  attr_values varchar(255) default '', 
  default_flag tinyint(1) default 0, 
  data_status tinyint(2) default 1,
  created_at datetime,
  updated_at datetime,
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists goods_category;
create table goods_category(  
  id int(11) not null auto_increment comment '主键',
  parent_id int default 0, 
  name varchar(100) not null, 
  remark  varchar(100),  
  created_at datetime,  
  updated_at datetime,  
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists goods_info;
create table goods_info(  
  id int(11) not null auto_increment comment '主键',
  goods_category_id int not null,
  goods_type tinyint(2) default 0,
  sale_flag tinyint(1) default 1,
  purchase_flag tinyint(1) default 1,
  name varchar(100) not null,
  code varchar(50) default '',
  bar_code varchar(50) default '',
  sales_volume decimal(10,2) default 0,
  main_unit_id int default  0,
  spec_flag tinyint(1) default 0,
  spec_name varchar(100) default '',
  supplier_info_id int default 0,
  stock_warn_flag tinyint(1) default 0,
  stock_warn_type tinyint(2) default 1,
  order_number decimal(10,2) default 0,
  discount_flag tinyint(1) default 1,
  out_sense_flag tinyint(1) default 0,
  modify_price_flag tinyint(1) default 0,
  weight decimal(10,2) default 0,
  data_status tinyint(2) default 1,
  remark  varchar(255), 
  created_at datetime, 
  updated_at datetime,  
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists goods_image_ref;
create table goods_image_ref(  
  id int(11) not null auto_increment comment '主键',
  goods_info_id int default 0,
  thumb varchar(255) not null,
  original varchar(255) not null,
  main_flag tinyint(1) default 0,
  position int default 1,
  created_at datetime,  
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists goods_stock_config;
create table goods_stock_config(  
  id int(11) not null auto_increment comment '主键',
  goods_info_id int default 0,
  spec_1_id int default 0,
  spec_option_1_id int default 0,
  spec_2_id int default 0,
  spec_option_2_id int default 0,
  spec_3_id int default 0,
  spec_option_3_id int default 0,
  unit_id int default 0,
  low_stock decimal(16,4) default 0,
  high_stock decimal(16,4) default 0,
  safe_stock decimal(16,4) default 0,
  created_at datetime,  
  updated_at datetime,  
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists goods_spec_ref;
create table goods_spec_ref(  
  id int(11) not null auto_increment comment '主键',
  goods_info_id int default 0,
  goods_spec_id int not null,
  spec_value varchar(255) not null,
  position int default 1,
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists goods_attribute_ref;
create table goods_attribute_ref(  
  id int(11) not null auto_increment comment '主键',
  goods_info_id int default 0,
  goods_attribute_id int not null,
  attr_value varchar(255) not null,
  position int default 1,
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;



drop table if exists goods_price;
create table goods_price(  
  id int(11) not null auto_increment comment '主键',
  goods_info_id int default 0,
  bar_code varchar(50), 
  spec_1_id int default 0,
  spec_option_1_id int default 0,
  spec_2_id int default 0,
  spec_option_2_id int default 0,
  spec_3_id int default 0,
  spec_option_3_id int default 0,
  unit_id int default 0,
  cost_price decimal(10,2) default 0,
  avg_cost_price decimal(10,2) default 0,
  init_price decimal(10,2) default 0,
  sale_price varchar(512), 
  data_status tinyint(2) default 1,
  remark  varchar(100), 
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists goods_print_tag;
create table goods_print_tag(  
  id int(11) not null auto_increment comment '主键',
   
  goods_info_id int default 0,
  bar_code varchar(50), 
  spec_1_id int default 0,
  spec_option_1_id int default 0,
  spec_2_id int default 0,
  spec_option_2_id int default 0,
  spec_3_id int default 0,
  spec_option_3_id int default 0,
  unit_id int default 0,
  goods_number int default 1,
  sale_price int default 0,
  remark  varchar(100), 
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists customer_info;
create table customer_info(  
  id int(11) not null auto_increment comment '主键',
   
  trader_book_account_id int default 0,
  customer_category_id int default 0,
  supplier_info_id int, 
  customer_price_level_id int default 0,
  default_flag tinyint(1) default 0,
  discount decimal(10,2) default 1,
  name varchar(100), 
  code varchar(20) not null, 
  make_man_id int default 0,
  last_man_id int default 0,
  handler_id int default 0,
  
  source_channel varchar(100) default '',
  contact varchar(100) default '', 
  mobile varchar(20) default '',
  moble_1 varchar(20) default '', 
  mobile_2 varchar(20) default '', 
  first_order_time datetime, 
  last_order_time datetime, 
  province_code varchar(100) default '',
  province_name varchar(100) default '',
  city_code varchar(100) default '',
  city_name varchar(100) default '',
  county_code varchar(100) default '',
  county_name varchar(100), 
  address varchar(255), 
  pay_type tinyint(2) default 1,
  pay_day int default 0,
  data_status tinyint(2) default 1,
  black_flag tinyint(1) default 0,
  credit_limit decimal(10,2) default 0,
  logistics_company_id int default 0,
  birthday datetime, 
  wechat varchar(50), 
  bank_name varchar(50), 
  bank_no varchar(50), 
  qq varchar(50), 
  weixin_flag tinyint(1) default 0,
  mall_auth_flag tinyint(1) default 1,
  remark varchar(255) default '', 
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists customer_category;
create table customer_category(  
  id int(11) not null auto_increment comment '主键',
   
  name varchar(100) not null, 
  remark varchar(255),  
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists customer_price_level;
create table customer_price_level(  
  id int(11) not null auto_increment comment '主键',
  name varchar(100) not null,
  data_status tinyint(2) default 1, 
  default_flag tinyint(1) default 0,
  remark varchar(255),  
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists inventory_checking;
create table inventory_checking(  
  id int(11) not null auto_increment comment '主键',
  order_code varchar(100) not null,
  make_man_id int default 0,
  last_man_id int default 0,
  handler_id int default 0,
  check_type tinyint(2) default 1,
  check_number decimal(10,2) default 0,
  differ_number decimal(10,2) default 0,
  profit_loss decimal(10,2) default 0,
  order_time datetime not null,
  order_status tinyint(2) default 0,
  audit_status tinyint(2) default 1,
  auditor_id int default 0,
  audit_time datetime, 
  audit_desc varchar(255),
  print_count smallint(4) default 0,
  remark varchar(100), 
  order_img varchar(512), 
   
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists inventory_checking_goods;
create table inventory_checking_goods(  
  id int(11) not null auto_increment comment '主键',
   
  inventory_checking_id int not null,
  goods_info_id int default 0,
  spec_1_id int default 0,
  spec_option_1_id int default 0,
  spec_2_id int default 0,
  spec_option_2_id int default 0,
  spec_3_id int default 0,
  spec_option_3_id int default 0,
  unit_id int default 0,
  check_number decimal(16,4) default 0,
  convert_number decimal(16,4) default 0,
  current_number decimal(16,4) default 0,
  profit_loss decimal(10,2) default 0,
  goods_batch_quality_id int default 0,
  batch varchar(50) default '', 
  quality varchar(50) default '', 
  remark  varchar(100), 
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists inventory_stock;
create table inventory_stock(  
  id int(11) not null auto_increment comment '主键',
  goods_info_id int not null,
  spec_1_id int, 
  spec_option_1_id int, 
  spec_2_id int, 
  spec_option_2_id int, 
  spec_3_id int, 
  spec_option_3_id int, 
  unit_id int, 
  init_stock decimal(16,4) default 0,
  stock decimal(16,4) default 0,
  lock_stock decimal(16,4) default 0,
  reserve_stock decimal(16,4) default 0,
  warn_type tinyint(2) default 0,
  data_status tinyint(2) default 1,
  remark  varchar(100), 
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;



drop table if exists inventory_stock_log;
create table inventory_stock_log(  
  id int(11) not null auto_increment comment '主键',
   
  goods_info_id int not null,
  spec_1_id int default 0,
  spec_option_1_id int default 0,
  spec_2_id int default 0,
  spec_option_2_id int default 0,
  spec_3_id int default 0,
  spec_option_3_id int default 0,
  unit_id int default 0,
  remain_number decimal(10,2) default 0,
  change_number decimal(10,2) default 0,
  io_type tinyint(2) default 1,
  order_type tinyint(2) default 1,
  order_id int default 0,
  order_code varchar(50) default '',
  remark  varchar(100), 
  created_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;



drop table if exists supplier_category;
create table supplier_category(  
  id int(11) not null auto_increment comment '主键',
   
  name varchar(100) not null,
  remark  varchar(100),
  created_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists supplier_info;
create table supplier_info(  
  id int(11) not null auto_increment comment '主键',
   
  trader_book_account_id int  not null,
  name varchar(50) not null,
  code varchar(20) not null,
  customer_info_id int,
  default_flag tinyint(1) default 0,
  data_status tinyint(2) default 1,
  black_flag tinyint(1) default 0,
  supplier_category_id int default 0,
  buyer_id int default 0,
  contact varchar(100), 
  mobile varchar(20), 
  first_order_time datetime, 
  last_order_time datetime, 
  province_code varchar(100) default '',
  province_name varchar(100) default '',
  city_code varchar(100) default '',
  city_name varchar(100) default '',
  county_code varchar(100) default '',
  county_name varchar(100), 
  address varchar(100), 
  bank_name varchar(100), 
  bank_account_name varchar(100), 
  bank_no varchar(100), 
  position smallint(4), 
  remark  varchar(100), 
  created_at datetime, 
  updated_at datetime,  
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists purchase_order;
create table purchase_order(  
  id int(11) not null auto_increment comment '主键',
  order_code varchar(100) not null,
  make_man_id int default 0,
  last_man_id int default 0,
  handler_id int default 0,
  supplier_info_id int default 0,
  goods_amount decimal(10,2) default 0,
  discount_type tinyint(2) default 1,
  discount decimal(10,2) default 1,
  discount_amount decimal(10,2) default 0,
  odd_amount decimal(10,2) default 0,
  other_amount decimal(10,2) default 0,
  other_cost_amount decimal(10,2) default 0,
  amount decimal(10,2) default 0,
  paid_amount decimal(10,2) default 0,
  order_status tinyint(2) default 0,
  pay_status tinyint(2) default 1,
  logistics_status tinyint(2) default 1,
  audit_status tinyint(2) default 1,
  auditor_id int default 0,
  audit_time datetime, 
  audit_desc varchar(255),
  reject_type tinyint(2) default 0,
  reject_amount decimal(10,2) default 0,
  order_time datetime not null,
  order_img varchar(512), 
  print_count smallint(4) default 0,
  remark  varchar(100), 
  cancel_remark varchar(100), 
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists purchase_order_goods;
create table purchase_order_goods(  
  id int(11) not null auto_increment comment '主键',
  
  supplier_info_id int not null,
  purchase_order_id int default 0,
  goods_info_id int default 0,
  spec_1_id int default 0,
  spec_option_1_id int default 0,
  spec_2_id int default 0,
  spec_option_2_id int default 0,
  spec_3_id int default 0,
  spec_option_3_id int default 0,
  unit_id int default 0,
  buy_number decimal(16,4) default 0,
  price decimal(16,4) default 0,
  discount decimal(10,2) default 1,
  discount_amount decimal(10,2) default 0,
  amount decimal(10,2) default 0,
  reject_type tinyint(2) default 0,
  reject_number decimal(16,4) default 0,
  reject_amount decimal(16,4) default 0,
  remark varchar(100), 
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists purchase_order_fee;
create table purchase_order_fee(  
  id int(11) not null auto_increment comment '主键',
  purchase_order_id int not null,
  trader_fund_type int not null,
  amount decimal(10,2) default 0,
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists purchase_order_cost;
create table purchase_order_cost(  
  id int(11) not null auto_increment comment '主键',
  purchase_order_id int not null,
  trader_fund_type int not null,
  trader_balance_account_id int default 0,
  amount decimal(10,2) default 0,
  remark varchar(100) default '',
  created_at datetime,
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists purchase_order_fund;
create table purchase_order_fund(  
  id int(11) not null auto_increment comment '主键',
  purchase_order_id int not null,
  balance_account_id int default 0,
  pay_type tinyint(2) default 0,
  amount decimal(10,2) default 0,
  pay_time datetime, 
  remark varchar(100) default '',
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists purchase_order_log;
create table purchase_order_log(  
  id int(11) not null auto_increment comment '主键',
  purchase_order_id int not null,
  oper_time datetime, 
  oper_desc varchar(2000) default '',
  oper_admin_name varchar(100) default '',
  oper_admin_id int  default 0,
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists purchase_reject_order;
create table purchase_reject_order(  
  id int(11) not null auto_increment comment '主键',
  purchase_order_id varchar(100) default '',
  order_code varchar(100) not null,
  make_man_id int default 0,
  last_man_id int default 0,
  handler_id int default 0,
  supplier_info_id int default 0,
  goods_amount decimal(10,2) default 0,
  amount decimal(10,2) default 0,
  paid_amount decimal(10,2) default 0,
  discount_type tinyint(2) default 1,
  discount decimal(10,2) default 1,
  discount_amount decimal(10,2) default 0,
  odd_amount decimal(10,2) default 0,
  other_amount decimal(10,2) default 0,
  other_cost_amount decimal(10,2) default 0,
  logistics_amount decimal(10,2) default 0,
  order_status tinyint(2) default 0,
  logistics_status tinyint(2) default 0,
  audit_status tinyint(2) default 1,
  auditor_id int default 0,
  audit_time datetime, 
  audit_desc varchar(255),
  pay_status tinyint(2) default 1,
  order_time datetime not null, 
  print_count  smallint(4)  default 0,
  remark  varchar(100), 
  cancel_remark varchar(100), 
  order_img varchar(512), 
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists purchase_reject_order_fee;
create table purchase_reject_order_fee(  
  id int(11) not null auto_increment comment '主键',
  purchase_reject_order_id int not null,
  trader_fund_type int not null,
  amount decimal(10,2) default 0,
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists purchase_reject_order_cost;
create table purchase_reject_order_cost(  
  id int(11) not null auto_increment comment '主键',
  purchase_reject_order_id int not null,
  trader_fund_type int not null,
  trader_balance_account_id int default 0,
  amount decimal(10,2) default 0,
  remark varchar(100) default '',
  created_at datetime,
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists purchase_reject_order_goods;
create table purchase_reject_order_goods(  
  id int(11) not null auto_increment comment '主键',
  supplier_info_id int not null,
  purchase_reject_order_id  int  default 0,
  purchase_order_goods_id  int  default 0,
  goods_info_id  int  default 0,
  spec_1_id  int  default 0,
  spec_option_1_id  int  default 0,
  spec_2_id  int  default 0,
  spec_option_2_id  int  default 0,
  spec_3_id  int  default 0,
  spec_option_3_id  int  default 0,
  unit_id  int  default 0,
  buy_number  decimal(10,2)  default 0,
  price  decimal(10,2)  default 0,
  discount  decimal(10,2)  default 1,
  discount_amount  decimal(10,2)  default 0,
  amount  decimal(10,2)  default 0,
  remark  varchar(100),  
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists purchase_reject_order_fund;
create table purchase_reject_order_fund(  
  id int(11) not null auto_increment comment '主键',
  purchase_reject_order_id int not null,
  balance_account_id int default 0,
  pay_type tinyint(2) default 0,
  amount decimal(10,2) default 0,
  pay_time datetime, 
  remark varchar(100) default '',
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists purchase_reject_order_log;
create table purchase_reject_order_log(  
  id int(11) not null auto_increment comment '主键',
  purchase_reject_order_id int not null,
  oper_time datetime, 
  oper_desc varchar(2000) default '',
  oper_admin_name varchar(100) default '',
  oper_admin_id int  default 0,
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists sale_order;
create table sale_order(  
  id int(11) not null auto_increment comment '主键',
  order_code varchar(100) not null,
  customer_info_id int not null,
  make_man_id int default 0,
  last_man_id int default 0,
  handler_id int default 0,
  goods_cost_amount decimal(10,2) default 0,
  goods_amount decimal(10,2) default 0,
  discount_type tinyint(2) default 1,
  discount decimal(10,2) default 1,
  discount_amount decimal(10,2) default 0,
  odd_amount decimal(10,2) default 0,
  other_amount decimal(10,2) default 0,
  other_cost_amount decimal(10,2) default 0,
  amount decimal(10,2) default 0,
  paid_amount decimal(10,2) default 0,
  cust_province_code varchar(100) default '',
  cust_province_name varchar(100) default '',
  cust_city_code varchar(100) default '',
  cust_city_name varchar(100) default '',
  cust_county_code varchar(100) default '',
  cust_county_name varchar(100) default '',
  cust_address varchar(255) default '',
  order_status tinyint(2) default 0,
  audit_status tinyint(2) default 1,
  auditor_id int default 0,
  audit_time datetime, 
  audit_desc varchar(255),
  pay_status tinyint(2) default 1,
  pay_type tinyint(2) default 1,
  urgent_flag tinyint(1) default 0, 
  logistics_status tinyint(2) default 0,
  logistics_service tinyint(2) default 0,
  delivery_distance varchar(100) default '',
  reject_type tinyint(2) default 0,
  reject_amount decimal(10,2) default 0,
  delivery_fee decimal(10,2) default 0,
  goods_unit int default 0,
  order_time datetime not null,
  print_count smallint(4) default 0,
  cancel_remark varchar(255), 
  purchase_order_id int default 0,
  purchase_order_code varchar(50)  default '',
  order_img varchar(512), 
  order_source tinyint(2) default 0,
  express_order  varchar(100) default '', 
  express_name  varchar(50) default '', 
  remark  varchar(100), 
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;




drop table if exists sale_order_goods;
create table sale_order_goods(  
  id int(11) not null auto_increment comment '主键',
  
  customer_info_id int not null,
  sale_order_id int default 0,
  goods_info_id int default 0,
  spec_1_id int default 0,
  spec_option_1_id int default 0,
  spec_2_id int default 0,
  spec_option_2_id int default 0,
  spec_3_id int default 0,
  spec_option_3_id int default 0,
  unit_id int default 0,
  buy_number decimal(16,4) default 0,
  price decimal(16,4) default 0,
  discount decimal(10,2) default 1,
  discount_amount decimal(10,2) default 0,
  amount decimal(10,2) default 0,
  cost_price decimal(10,2) default 0,
  cost_amount decimal(10,2) default 0,
  reject_type tinyint(2) default 0,
  reject_number decimal(16,4) default 0,
  reject_amount decimal(10,2) default 0,
  goods_count int default 0,
  supplier_info_id int default 0,
  remark varchar(100), 
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists sale_order_fee;
create table sale_order_fee(  
  id int(11) not null auto_increment comment '主键',
  sale_order_id int default 0,
  trader_fund_type int not null,
  amount decimal(10,2) default 0,
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists sale_order_cost;
create table sale_order_cost(  
  id int(11) not null auto_increment comment '主键',
  
  sale_order_id int default 0,
  trader_fund_type int not null,
  trader_balance_account_id int default 0,
  amount decimal(10,2) default 0,
  remark varchar(100) default '',
  created_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists sale_order_fund;
create table sale_order_fund(  
  id int(11) not null auto_increment comment '主键',
  sale_order_id int default 0,
  balance_account_id int default 0,
  receipt_type tinyint(2) default 0,
  amount decimal(10,2) default 0,
  receipt_time datetime, 
  remark varchar(100) default '',
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists sale_order_log;
create table sale_order_log(  
  id int(11) not null auto_increment comment '主键',
  
  sale_order_id int not null,
  oper_time datetime, 
  oper_desc varchar(2000) default '',
  oper_admin_name varchar(100) default '',
  oper_admin_id int  default 0,
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists sale_reject_order;
create table sale_reject_order(  
  id int(11) not null auto_increment comment '主键',
  order_code varchar(100) not null,
  
  customer_info_id int not null,
  sale_order_id varchar(100) default '',
  make_man_id int default 0,
  last_man_id int default 0,
  handler_id int default 0,
  goods_amount decimal(10,2) default 0,
  amount decimal(10,2) default 0,
  paid_amount decimal(10,2) default 0,
  discount_type tinyint(2) default 1,
  discount decimal(10,2) default 1,
  discount_amount decimal(10,2) default 0,
  odd_amount decimal(10,2) default 0,
  other_amount decimal(10,2) default 0,
  other_cost_amount decimal(10,2) default 0,
  logistics_amount decimal(10,2) default 0,
  order_status tinyint(2) default 0,
  audit_status tinyint(2) default 1,
  auditor_id int default 0,
  audit_time datetime, 
  audit_desc varchar(255),
  pay_status tinyint(2) default 1,
  logistics_status tinyint(2) default 1,
  goods_unit int default 0,
  reject_reason_type tinyint(2) default 0,
  print_count smallint(4) default 0,
  order_time datetime not null,
  order_img varchar(512), 
  remark  varchar(100), 
  
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;



drop table if exists sale_reject_order_goods;
create table sale_reject_order_goods(  
  id int(11) not null auto_increment comment '主键',
  
  customer_info_id int not null,
  sale_reject_order_id int default 0,
  goods_info_id int default 0,
  spec_1_id int default 0,
  spec_option_1_id int default 0,
  spec_2_id int default 0,
  spec_option_2_id int default 0,
  spec_3_id int default 0,
  spec_option_3_id int default 0,
  unit_id int default 0,
  buy_number decimal(16,4) default 0,
  price decimal(16,4) default 0,
  discount decimal(10,2) default 1,
  discount_amount decimal(10,2) default 0,
  amount decimal(10,2) default 0,
  cost_price decimal(10,2) default 0,
  cost_amount decimal(10,2) default 0,
  reject_deal_type tinyint(2) default 1,
  remark  varchar(100), 
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists sale_reject_order_fee;
create table sale_reject_order_fee(  
  id int(11) not null auto_increment comment '主键',
  sale_reject_order_id int default 0,
  trader_fund_type int not null,
  amount decimal(10,2) default 0,
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists sale_reject_order_cost;
create table sale_reject_order_cost(  
  id int(11) not null auto_increment comment '主键',
  sale_reject_order_id int default 0,
  trader_fund_type int not null,
  trader_balance_account_id int default 0,
  amount decimal(10,2) default 0,
  remark varchar(100) default '',
  created_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists sale_reject_order_fund;
create table sale_reject_order_fund(  
  id int(11) not null auto_increment comment '主键',
  sale_reject_order_id int not null,
  balance_account_id int default 0,
  receipt_type tinyint(2) default 0,
  amount decimal(10,2) default 0,
  receipt_time datetime, 
  remark varchar(100) default '', 
  created_at datetime,  
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;



drop table if exists sale_reject_order_log;
create table sale_reject_order_log(  
  id int(11) not null auto_increment comment '主键',
  sale_reject_order_id int not null,
  oper_time datetime, 
  oper_desc varchar(2000) default '',
  oper_admin_name varchar(100) default '',
  oper_admin_id int  default 0,
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists trader_fund_type;
create table trader_fund_type(  
  id int(11) not null auto_increment comment '主键',
  name varchar(50) not null,
  fund_flow tinyint(2) not null,
  postion smallint(4) default 100,
  data_status tinyint(2) default 1,
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists trader_balance_account;
create table trader_balance_account(  
  id int(11) not null auto_increment comment '主键',
  name varchar(50) not null,
  code varchar(20) default '',
  account_no varchar(50) default '',
  account_name varchar(50) default '',
  position tinyint(2) default 100,
  default_flag tinyint(1) default 0,
  data_status tinyint(2) default 1,
  balance decimal(10,2) default 0,
  remark varchar(255), 
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;




drop table if exists trader_book_account;
create table trader_book_account(  
  id int(11) not null auto_increment comment '主键',
  in_amount decimal(10,2) default 0,
  out_amount decimal(10,2) default 0,
  pay_amount decimal(10,2) default 0,
  open_balance decimal(10,2) default 0,
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists trader_book_account_logs;
create table trader_book_account_logs(  
  id int(11) not null auto_increment comment '主键',
  trader_book_account_id int not null,
  fund_flow tinyint(2) not null,
  amount decimal(10,2) not null,
  balance decimal(10,2) default 0,
  remark varchar(255), 
  created_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists trader_pay_order;
create table trader_pay_order(  
  id int(11) not null auto_increment comment '主键',
  supplier_info_id int default 0,
  make_man_id int default 0,
  last_man_id int default 0,
  handler_id int default 0,
  order_code varchar(20) not null,
  order_amount decimal(10,2) default 0,
  discount_amount decimal(10,2) default 0,
  check_amount decimal(10,2) default 0,
  amount decimal(10,2) default 0,
  order_time datetime not null,
  order_img varchar(512), 
  order_status tinyint(2) default 0,
  audit_status tinyint(2) default 1,
  auditor_id int default 0,
  audit_time datetime, 
  audit_desc varchar(255), 
  print_count smallint(4) default 0,
  remark varchar(255), 
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists trader_pay_order_fund;
create table trader_pay_order_fund(  
  id int(11) not null auto_increment comment '主键',
  trader_pay_order_id  int  not null,
  trader_balance_account_id  int  not null,
  amount  decimal(10,2)  not null,
  fund_time  datetime,
  remark varchar(100) default '', 
  created_at  datetime,
  updated_at  datetime,
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists trader_pay_order_ref;
create table trader_pay_order_ref(  
  id int(11) not null auto_increment comment '主键',
  trader_pay_order_id int not null,
  purchase_order_id int not null,
  amount decimal(10,2) not null,
  discount_amount decimal(10,2) default 0,
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists trader_receipt_order;
create table trader_receipt_order(  
  id int(11) not null auto_increment comment '主键',
  customer_info_id int not null,
  make_man_id int default 0,
  last_man_id int default 0,
  handler_id int default 0,
  order_code varchar(20) not null,
  order_amount decimal(10,2) default 0,
  discount_amount decimal(10,2) default 0,
  check_amount decimal(10,2) default 0,
  amount decimal(10,2) default 0,
  order_time datetime not null,
  order_img varchar(512), 
  order_status tinyint(2) default 0,
  audit_status tinyint(2) default 1,
  auditor_id int default 0,
  audit_time datetime, 
  audit_desc varchar(255), 
  print_count smallint(4) default 0,
  remark varchar(255), 
  created_at datetime,
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists trader_receipt_order_fund;
create table trader_receipt_order_fund(  
  id int(11) not null auto_increment comment '主键',
  trader_receipt_order_id  int  not null,
  trader_balance_account_id  int  not null,
  amount  decimal(10,2)  not null,
  fund_time  datetime,
  remark varchar(100) default '', 
  created_at  datetime,
  updated_at  datetime,
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists trader_receipt_order_ref;
create table trader_receipt_order_ref(  
  id int(11) not null auto_increment comment '主键',
  trader_receipt_order_id  int  not null,
  sale_order_id int not null,
  amount decimal(10,2) not null,
  discount_amount decimal(10,2) default 0,
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists trader_income_expenses;
create table trader_income_expenses(  
  id int(11) not null auto_increment comment '主键',
  make_man_id int default 0,
  last_man_id int default 0,
  handler_id int default 0,
  fund_flow tinyint(2) not null,
  fund_type_id int not null,
  order_code varchar(20) not null,
  amount  decimal(10,2)  not null,
  order_time datetime not null,
  order_img varchar(512), 
  order_status tinyint(2) default 0,
  audit_status tinyint(2) default 1,
  auditor_id int default 0,
  audit_time datetime, 
  audit_desc varchar(255), 
  remark varchar(255), 
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists trader_income_expenses_fund;
create table trader_income_expenses_fund(  
  id int(11) not null auto_increment comment '主键',
  trader_income_expenses_id  int  not null,
  trader_balance_account_id  int  not null,
  amount  decimal(10,2)  not null,
  fund_time  datetime,
  remark varchar(100) default '', 
  created_at  datetime,
  updated_at  datetime,
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists trader_transfer_order;
create table trader_transfer_order(  
  id int(11) not null auto_increment comment '主键',
  out_account_id int not null,
  out_time datetime, 
  in_account_id int not null,
  in_time datetime, 
  make_man_id int default 0,
  last_man_id int default 0,
  handler_id int default 0,
  order_code varchar(20) not null,
  amount decimal(10,2) not null,
  fee decimal(10,2) default 0,
  fee_pay_account tinyint(2) default 1,
  order_status tinyint(2) default 0,
  audit_status tinyint(2) default 1,
  auditor_id int default 0,
  audit_time datetime, 
  audit_desc varchar(255), 
  remark varchar(255), 
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;

drop table if exists trader_fund_order;
create table trader_fund_order(  
  id int(11) not null auto_increment comment '主键',
  trader_balance_account_id int not null,
  ref_order_type tinyint(2) not null,
  ref_order_id int default 0,
  ref_order_code varchar(20) default '',
  amount decimal(10,2) not null,
  fund_flow tinyint(2) not null,
  order_time datetime not null,
  remark varchar(255) default '',
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;



drop table if exists trader_customer_receivable;
create table trader_customer_receivable(  
  id int(11) not null auto_increment comment '主键',
  customer_info_id int not null,
  trader_book_account_id int not null,
  ref_order_type tinyint(2) not null,
  ref_order_id int default 0,
  ref_order_code varchar(20) default '',
  new_amount decimal(10,2) default 0,
  take_amount decimal(10,2) default 0,
  discount_amount decimal(10,2) default 0,
  adjust_amount decimal(10,2) default 0,
  order_time datetime not null,
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;


drop table if exists trader_supplier_payable;
create table trader_supplier_payable(  
  id int(11) not null auto_increment comment '主键',
  supplier_info_id int not null,
  trader_book_account_id int not null,
  ref_order_type tinyint(2) not null,
  ref_order_id int default 0,
  ref_order_code varchar(20) default '',
  new_amount decimal(10,2) default 0,
  take_amount decimal(10,2) default 0,
  discount_amount decimal(10,2) default 0,
  adjust_amount decimal(10,2) default 0,
  order_time datetime not null,
  created_at datetime, 
  updated_at datetime, 
  primary key (id)
) engine=innodb charset=utf8mb4 collate=utf8mb4_general_ci;
