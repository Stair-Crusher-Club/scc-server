provider "aws" {
  region = "us-east-1"
}

terraform {
  backend "s3" {
    bucket = "scc-prod-tf-remote-state"
    key    = "ecr.tfstate"
    region = "ap-northeast-2"
  }
}

