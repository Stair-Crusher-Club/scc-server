terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
    }
    awscc = {
      source = "hashicorp/awscc"
    }
    sops = {
      source = "carlpett/sops"
    }
  }

  backend "s3" {
    bucket = "scc-prod-tf-remote-state"
    key    = "database.tfstate"
    region = "ap-northeast-2"
  }
}

provider "aws" {
  region = "ap-northeast-2"
}

provider "awscc" {
  region = "ap-northeast-2"
}

provider "sops" {}

data "sops_file" "secret_data" {
  source_file = "secret.yaml"
}

