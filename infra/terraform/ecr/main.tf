provider "aws" {
  region = "ap-northeast-2"
}

# Alias provider specifically for ECR Public operations
# Delete when public repositories are no longer needed
provider "aws" {
  alias  = "us-east-1"
  region = "us-east-1"
}

terraform {
  backend "s3" {
    bucket = "scc-prod-tf-remote-state"
    key    = "ecr.tfstate"
    region = "ap-northeast-2"
  }
}

