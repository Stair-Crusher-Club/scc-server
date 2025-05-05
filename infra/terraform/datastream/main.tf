terraform {
  required_version = ">= 1.3.0"

  required_providers {
    aws = {
      source = "hashicorp/aws"
    }

    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }

    postgresql = {
      source  = "cyrilgdn/postgresql"
      version = "~> 1.20"
    }

    sops = {
      source = "carlpett/sops"
    }
  }

  backend "s3" {
    bucket = "scc-prod-tf-remote-state"
    key    = "datastream.tfstate"
    region = "ap-northeast-2"
  }
}

provider "sops" {}

data "sops_file" "secret_data" {
  source_file = "secret.yaml"
}

provider "google" {
  project     = var.project_id
  region      = var.region
  credentials = data.sops_file.secret_data.data["google-credentials-json"]
}

provider "postgresql" {
  host      = data.terraform_remote_state.database.outputs.scc_rds_instance_host
  port      = var.rds_postgres_port
  database  = var.rds_postgres_database
  username  = var.rds_postgresql_master_username
  password  = data.sops_file.secret_data.data["rds-postgresql-master-password"]
  sslmode   = "require"
  superuser = false
}

data "terraform_remote_state" "database" {
  backend = "s3"

  config = {
    bucket = "scc-prod-tf-remote-state"
    key    = "database.tfstate"
    region = "ap-northeast-2"
  }
}
