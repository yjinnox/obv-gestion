{{/*
Nom de base du chart/release.
*/}}
{{- define "obv-gestion.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Nom complet qualifié (release + chart), utilisé pour les ressources qui
n'ont pas de nom de Service imposé par convention (backend/frontend).
*/}}
{{- define "obv-gestion.fullname" -}}
{{- printf "%s-%s" .Release.Name (include "obv-gestion.name" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "obv-gestion.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Étiquettes communes (recommandations Helm/Kubernetes).
*/}}
{{- define "obv-gestion.labels" -}}
helm.sh/chart: {{ include "obv-gestion.chart" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/part-of: {{ include "obv-gestion.name" . }}
{{- end -}}

{{- define "obv-gestion.backend.labels" -}}
{{ include "obv-gestion.labels" . }}
app.kubernetes.io/name: {{ .Values.backend.serviceName }}
app.kubernetes.io/component: backend
{{- end -}}

{{- define "obv-gestion.backend.selectorLabels" -}}
app.kubernetes.io/name: {{ .Values.backend.serviceName }}
app.kubernetes.io/component: backend
{{- end -}}

{{- define "obv-gestion.frontend.labels" -}}
{{ include "obv-gestion.labels" . }}
app.kubernetes.io/name: {{ .Values.frontend.serviceName }}
app.kubernetes.io/component: frontend
{{- end -}}

{{- define "obv-gestion.frontend.selectorLabels" -}}
app.kubernetes.io/name: {{ .Values.frontend.serviceName }}
app.kubernetes.io/component: frontend
{{- end -}}

{{- define "obv-gestion.secretName" -}}
{{- if .Values.secrets.existingSecretName -}}
{{- .Values.secrets.existingSecretName -}}
{{- else -}}
{{- printf "%s-secrets" (include "obv-gestion.fullname" .) -}}
{{- end -}}
{{- end -}}
