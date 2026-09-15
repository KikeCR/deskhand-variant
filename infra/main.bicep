// Provisions the Azure resources this project needs to run in production: an App Service Plan +
// Web App (Java SE runtime, running the Spring Boot jar directly - no container/registry needed),
// wired up with the same environment variables documented in .env.example.
//
// This does NOT provision the Azure OpenAI or Azure AI Search resources themselves - those are
// created separately (see README's "Where do I get these" section) since they involve model
// deployment/quota steps that don't fit cleanly into a single reusable Bicep module for a
// portfolio-scale project. This template wires an *existing* pair of those resources' endpoint/key
// values into the Web App's app settings.
//
// NOT YET VERIFIED against a real deployment (flagged per project convention - see README's "Known
// gaps"): the exact `linuxFxVersion` string for Java 21 SE on Linux App Service. Run
//   az webapp list-runtimes --os linux | grep JAVA
// and compare against the `javaLinuxFxVersion` parameter default below BEFORE deploying - Azure's
// Java SE runtime string format has shifted before (e.g. 'JAVA|17-java17' for Java 17), and the
// exact Java 21 spelling was not confirmed from a single authoritative source during authoring.

@description('Azure region for all resources.')
param location string = resourceGroup().location

@description('Base name used to derive resource names (App Service Plan, Web App).')
param appName string = 'deskhand-variant'

@description('App Service Plan SKU - B1 is the cheapest tier that supports always-on and custom domains; F1 (free) does not support always-on, which a portfolio demo you want to reliably show working benefits from.')
param appServicePlanSku string = 'B1'

@description('Linux Java SE runtime stack string. VERIFY this against `az webapp list-runtimes --os linux | grep JAVA` before deploying - see the file header comment.')
param javaLinuxFxVersion string = 'JAVA|21-java21'

@description('Azure OpenAI resource endpoint, e.g. https://<resource-name>.openai.azure.com/')
param azureOpenAiEndpoint string

@secure()
@description('Azure OpenAI API key.')
param azureOpenAiApiKey string

param azureOpenAiChatDeployment string = 'gpt-4o-mini'
param azureOpenAiEmbeddingDeployment string = 'text-embedding-3-small'
param azureOpenAiEmbeddingDimensions int = 1536

@description('Azure AI Search resource endpoint, e.g. https://<service-name>.search.windows.net')
param azureSearchEndpoint string

@secure()
@description('Azure AI Search admin key.')
param azureSearchApiKey string

param azureSearchIndexName string = 'company-docs'

resource appServicePlan 'Microsoft.Web/serverfarms@2024-04-01' = {
  name: '${appName}-plan'
  location: location
  kind: 'linux'
  sku: {
    name: appServicePlanSku
  }
  properties: {
    reserved: true // required for Linux plans
  }
}

resource webApp 'Microsoft.Web/sites@2024-04-01' = {
  name: appName
  location: location
  properties: {
    serverFarmId: appServicePlan.id
    httpsOnly: true
    siteConfig: {
      linuxFxVersion: javaLinuxFxVersion
      minTlsVersion: '1.2'
      ftpsState: 'Disabled'
      alwaysOn: true
      // App Service's Java SE runtime expects port 80 by default; Spring Boot listens on 8080
      // (see application.yml) - WEBSITES_PORT tells the platform where to route requests.
      // Onboarding runs take 15-40s; the platform's own request timeout is generous by default,
      // but keepAliveTimeout/webSocketsEnabled tuning is not needed for this synchronous-only API.
      appSettings: [
        { name: 'WEBSITES_PORT', value: '8080' }
        { name: 'AZURE_OPENAI_ENDPOINT', value: azureOpenAiEndpoint }
        { name: 'AZURE_OPENAI_API_KEY', value: azureOpenAiApiKey }
        { name: 'AZURE_OPENAI_CHAT_DEPLOYMENT', value: azureOpenAiChatDeployment }
        { name: 'AZURE_OPENAI_EMBEDDING_DEPLOYMENT', value: azureOpenAiEmbeddingDeployment }
        { name: 'AZURE_OPENAI_EMBEDDING_DIMENSIONS', value: string(azureOpenAiEmbeddingDimensions) }
        { name: 'AZURE_SEARCH_ENDPOINT', value: azureSearchEndpoint }
        { name: 'AZURE_SEARCH_API_KEY', value: azureSearchApiKey }
        { name: 'AZURE_SEARCH_INDEX_NAME', value: azureSearchIndexName }
      ]
    }
  }
}

@description('The deployed web app default hostname - the API base URL.')
output webAppUrl string = 'https://${webApp.properties.defaultHostName}'

@description('The web app name, needed for `az webapp deploy` to push the built jar.')
output webAppName string = webApp.name
