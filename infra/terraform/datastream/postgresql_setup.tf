resource "random_password" "scc_rds_datastream_user_password" {
  length  = 20
  special = false
}

# FIXME: 현재 postgresql provider의 한계로 인해, replication membership role을 terraform으로 줄 수 없다.
#        따라서 terraform apply를 할 때마다 아래 쿼리를 통해 replication membership role을 제공해줘야 한다.
#        GRANT SELECT ON ALL TABLES IN SCHEMA public TO bigquery_datastream_user;
resource "postgresql_role" "scc_rds_datastream_user" {
  name     = var.scc_rds_datastream_user_username
  password = random_password.scc_rds_datastream_user_password.result

  login = true

  # 수기로 rds_replication membership role을 지급해서 발생한 diff를 무시하도록 설정한다.
  lifecycle {
    ignore_changes = [
      roles,
    ]
  }
}

resource "postgresql_grant" "scc_rds_datastream_user_usage" {
  database    = var.rds_postgres_database
  role        = postgresql_role.scc_rds_datastream_user.name
  object_type = "database"
  privileges  = ["CONNECT"]
}

# FIXME: 이 권한은 "앞으로 생성될" 테이블에 대해 SELECT 권한을 기본으로 제공하도록 설정하는 것이고,
#        이 리소스를 생성할 때 이미 존재하는 테이블에 대한 SELECT 권한을 주지는 못한다.
#        따라서 아래 쿼리를 별도로 실행해줘야 한다.
#        GRANT SELECT ON ALL TABLES IN SCHEMA public TO datastream_user_xyz;
resource "postgresql_default_privileges" "scc_rds_datastream_user_select" {
  role        = postgresql_role.scc_rds_datastream_user.name
  owner       = var.rds_postgresql_master_username
  privileges  = ["SELECT"]
  database    = var.rds_postgres_database
  object_type = "table"
}

resource "postgresql_publication" "scc_rds_datastream_publication" {
  name  = "bigquery_datastream_publication"
}

resource "postgresql_replication_slot" "scc_rds_datastream_replication_slot" {
  name  = "bigquery_datastream_slot"
  plugin = "pgoutput"
}
