data "aws_s3_bucket" "videos_to_process" {
  bucket = "fase5-videos-to-process"
}

data "aws_s3_bucket" "processed_frames" {
  bucket = "fase5-processed-frames"
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

  # # CloudWatch Logs permissions for Fluent Bit
  # statement {
  #   effect = "Allow"
  #   actions = [
  #     "logs:CreateLogStream",
  #     "logs:CreateLogGroup",
  #     "logs:PutLogEvents",
  #     "logs:DescribeLogStreams",
  #     "logs:DescribeLogGroups"
  #   ]
  #   resources = [
  #     "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:/aws/eks/${data.aws_eks_cluster.cluster.name}/${var.project_name}",
  #     "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:/aws/eks/${data.aws_eks_cluster.cluster.name}/${var.project_name}:*"
  #   ]
  # }

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
      "s3:GetObject",
      "s3:PutObject"
    ]
    resources = [
      "${data.aws_s3_bucket.processed_frames.arn}/*"
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
      "dynamodb:Scan",
      "dynamodb:Query"
    ]
    resources = [
      data.aws_dynamodb_table.video_processing_table.arn,
      "${data.aws_dynamodb_table.video_processing_table.arn}/index/*"
    ]
  }
}



resource "aws_iam_role_policy" "app_policy" {
  name   = "${var.project_name}-policy"
  role   = aws_iam_role.app_role.id
  policy = data.aws_iam_policy_document.app_policy.json
}