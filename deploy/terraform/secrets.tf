resource "aws_secretsmanager_secret" "secrets" {
  name                    = "${var.project_name}-secrets"
  description             = "Secrets for ${var.project_name} project"
  recovery_window_in_days = 0

  tags = {
    Name = "${var.project_name}-secrets"
  }
}

resource "aws_secretsmanager_secret_version" "secrets" {
  secret_id = aws_secretsmanager_secret.secrets.id
  secret_string = jsonencode({
    "fase5.notification.worker.cognito_jwk_url" = local.aws_infra_secrets["COGNITO_JWKS_URL"]
  })
}
