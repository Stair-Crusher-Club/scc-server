terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
    }
    sops = {
      source = "carlpett/sops"
    }
  }

  backend "s3" {
    bucket = "scc-tf-remote-state"
    key    = "lightsail.tfstate"
    region = "ap-northeast-2"
  }
}

provider "aws" {
  region = "ap-northeast-2"
}

provider "sops" {}

data "sops_file" "secret_data" {
  source_file = "secret.yaml"
}

