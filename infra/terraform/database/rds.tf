data "aws_vpc" "default_vpc" {
  id = "vpc-03c009415dbb040ad"
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

  apply_immediately = true
  auto_minor_version_upgrade = false
  skip_final_snapshot = false
  final_snapshot_identifier = "scc-rds-snapshot"
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
