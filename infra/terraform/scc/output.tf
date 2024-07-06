output "dev_cdn_endpoint" {
  value = aws_cloudfront_distribution.scc_dev_distribution.domain_name
}

output "cdn_endpoint" {
  value = aws_cloudfront_distribution.scc_distribution.domain_name
}
