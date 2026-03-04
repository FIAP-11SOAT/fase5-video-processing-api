terraform {
  backend "s3" {
    bucket = "fase5-terraform-state"
    key    = "fase5-video-processing-api/terraform.tfstate"
    region = "us-east-1"
  }

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}
