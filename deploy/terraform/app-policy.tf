data "aws_s3_bucket" "videos_to_process" {
  bucket = "fase5-videos-to-process"
}

data "aws_sqs_queue" "video_notification_queue" {
  name = "fase5-video-notification-queue"
}

data "aws_dynamodb_table" "video_processing_table" {
  name = "fase5-video-processing"
}


data "aws_iam_policy_document" "app_policy" {

  statement {
    effect = "Allow"
    actions = [
      "secretsmanager:GetSecretValue",
      "secretsmanager:DescribeSecret"
    ]
    resources = [
      aws_secretsmanager_secret.secrets.arn
    ]
  }

  statement {
    effect = "Allow"
    actions = [
      "s3:GetObject",
      "s3:PutObject"
    ]
    resources = [
      "${data.aws_s3_bucket.videos_to_process.arn}/*"
    ]
  }

  statement {
    effect = "Allow"
    actions = [
      "sqs:SendMessage",
      "sqs:GetQueueUrl",
      "sqs:GetQueueAttributes"
    ]
    resources = [
      data.aws_sqs_queue.video_notification_queue.arn
    ]
  }

  statement {
    effect = "Allow"
    actions = [
      "dynamodb:PutItem",
      "dynamodb:GetItem",
      "dynamodb:UpdateItem",
      "dynamodb:Scan"
    ]
    resources = [
      data.aws_dynamodb_table.video_processing_table.arn
    ]
  }
}



resource "aws_iam_role_policy" "app_policy" {
  name   = "${var.project_name}-policy"
  role   = aws_iam_role.app_role.id
  policy = data.aws_iam_policy_document.app_policy.json
}