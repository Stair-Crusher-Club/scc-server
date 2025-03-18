provider "aws" {
  region = "ap-northeast-2"
}

terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
    }
  }

  backend "s3" {
    bucket = "scc-prod-tf-remote-state"
    key    = "hot-updater.tfstate"
    region = "ap-northeast-2"
  }
}
