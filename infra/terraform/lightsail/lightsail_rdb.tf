resource "aws_lightsail_database" "scc_db" {
  relational_database_name = "scc-db-1"
  availability_zone        = "ap-northeast-2a"
  master_database_name     = "scc"
  master_password          = data.sops_file.secret_data.data["db.password"]
  master_username          = "scc"
  blueprint_id             = "postgres_12"
  bundle_id                = "micro_2_0"
  skip_final_snapshot      = true
}

