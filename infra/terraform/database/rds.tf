data "aws_vpc" "default_vpc" {
  id = "vpc-03c009415dbb040ad"
}

resource "aws_db_parameter_group" "scc_rds" {
  name        = "scc-rds"
  family      = "postgres15"
  description = "Parameter group for scc-rds RDS instance"

  # 데이터베이스를 빅쿼리에 붓기 위한 logical replication 설정.
  parameter {
    name  = "rds.logical_replication"
    value = "1"
    apply_method = "pending-reboot"
  }

  # Optional but often needed for logical replication to work properly
  parameter {
    name  = "max_replication_slots"
    value = "10"
    apply_method = "pending-reboot"
  }

  parameter {
    name  = "max_wal_senders"
    value = "10"
    apply_method = "pending-reboot"
  }
}

resource "aws_db_instance" "scc_rds" {
  identifier = "scc-rds"

  allocated_storage = 120
  storage_type = "gp2"
  db_name = "scc"
  engine = "postgres"
  engine_version = "15.10"
  instance_class = "db.t3.medium"
  username = "scc"
  password = data.sops_file.secret_data.data["db.password"]
  publicly_accessible = true
  vpc_security_group_ids = [aws_security_group.scc_rds_security_group.id]

  backup_retention_period = 1
  backup_window = "17:00-18:00"
  maintenance_window = "sun:18:00-sun:19:00"

  performance_insights_enabled = true

  apply_immediately = false
  auto_minor_version_upgrade = false
  skip_final_snapshot = false
  final_snapshot_identifier = "scc-rds-snapshot"

  deletion_protection = true

  parameter_group_name = aws_db_parameter_group.scc_rds.name
}

resource "aws_security_group" "scc_rds_security_group" {
  name        = "scc-rds-sg"
  description = "Security group of scc rds"
  vpc_id      = data.aws_vpc.default_vpc.id
}

resource "aws_vpc_security_group_egress_rule" "rds_allow_all_ipv4" {
  security_group_id = aws_security_group.scc_rds_security_group.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
}

resource "aws_vpc_security_group_ingress_rule" "rds_allow_tcp_ipv4" {
  security_group_id = aws_security_group.scc_rds_security_group.id
  cidr_ipv4         = "0.0.0.0/0"
  from_port         = 5432
  ip_protocol       = "tcp"
  to_port           = 5432
}
