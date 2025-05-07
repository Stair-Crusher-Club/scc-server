variable "project_id" {
  description = "GCP project ID"
  type        = string
  default     = "staircrusherclub"
}

variable "region" {
  description = "GCP region"
  type        = string
  default     = "asia-northeast3"
}

variable "rds_postgresql_master_username" {
  description = "Master user for PostgreSQL (superuser privileges)"
  type        = string
  default     = "scc"
}

variable "rds_postgres_port" {
  description = "PostgreSQL port"
  type        = number
  default     = 5432
}

variable "rds_postgres_database" {
  description = "PostgreSQL database name"
  type        = string
  default     = "scc"
}

variable "scc_rds_datastream_user_username" {
  description = "Replication username"
  type        = string
  default     = "bigquery_datastream_user"
}
