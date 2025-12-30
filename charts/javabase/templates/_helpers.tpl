{{/*
Expand the name of the chart.
*/}}
{{- define "javabase.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "javabase.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "javabase.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "javabase.labels" -}}
helm.sh/chart: {{ include "javabase.chart" . }}
{{ include "javabase.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "javabase.selectorLabels" -}}
app.kubernetes.io/name: {{ include "javabase.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "javabase.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "javabase.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Create the name of the secret to use for database credentials
*/}}
{{- define "javabase.databaseSecretName" -}}
{{- if .Values.database.existingSecret }}
{{- .Values.database.existingSecret }}
{{- else }}
{{- include "javabase.fullname" . }}-db
{{- end }}
{{- end }}

{{/*
Create the name of the secret to use for RabbitMQ credentials
*/}}
{{- define "javabase.rabbitmqSecretName" -}}
{{- if .Values.rabbitmq.existingSecret }}
{{- .Values.rabbitmq.existingSecret }}
{{- else }}
{{- include "javabase.fullname" . }}-rabbitmq
{{- end }}
{{- end }}
