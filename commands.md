az aks start --name aks-learn --resource-group rg-learn-aks

az aks stop --name aks-learn --resource-group rg-learn-aks

az group delete --name rg-learn-aks --yes --no-wait

az vm list-skus \
--location centralindia \
--size Standard_D2s_v5 \
--output table



az aks create \
--resource-group rg-learn-aks \
--name aks-learn \
--location centralindia \
--node-vm-size Standard_D2s_v5 \
--node-count 1 \
--attach-acr acrlearn4471 \
--enable-managed-identity \
--generate-ssh-keys


az quota create \
--scope /subscriptions/$(az account show --query id -o tsv)/providers/Microsoft.Compute/locations/centralindia \
--resource-name standardDSv5Family \
--limit-object value=4 \
--output table



