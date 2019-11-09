# Running Kotlink on Minikube (using YAML)

**Disclaimer:** the deployment steps provided here are a reference point 
and should not be applied in any sort of production environment. 
Namely, the setup for Redis / PostgreSQL is extremely simplistic 
and does not take HA / scaling into account. 

## Instructions

Set up minikube:
```shell script
minikube start --memory 16gb --cpus 8 --disk-size 64gb --vm-driver kvm2
minikube addons enable metrics-server
minikube addons enable ingress
minikube addons enable dashboard
kubectx minikube
```

Create namespace:
```shell script
kubectl apply -f namespace.yaml
kubens kotlink
```

Set up standalone PostgreSQL:
```shell script
kubectl apply -f postgres/secret.yaml -f postgres/statefulset.yaml -f postgres/service.yaml
```

Set up standalone Redis:
```shell script
kubectl apply -f redis/deployment.yaml -f redis/service.yaml
```

Set up Kotlink:
```shell script
kubectl apply -f kotlink/secret.yaml \
              -f kotlink/deployment.yaml \
              -f kotlink/hpa.yaml \
              -f kotlink/service.yaml \
              -f kotlink/ingress.yaml
```

Access Kotlink in your browser:
* Run `minikube ip` and add the resulting IP into `/etc/hosts` 
(e.g. `192.168.99.100 local.kotlink.org`)
* Open [local.kotlink.org](https://local.kotlink.org)

## How-Tos

To access Redis:
* Run `kubectl port-forward service/redis 6379:6379` in one Terminal
* In a different terminal run `redis-cli`

To access Postgres:
* Run `kubectl port-forward service/postgres 5432:5432` in one Terminal
* In a different terminal first get the password: 
`kubectl get secret --namespace kotlink postgres -o jsonpath="{.data.password}" | base64 --decode`
* Then, run `psql -h 127.0.0.1 -p 5432 -U kotlinkuser -W kotlink` (use the password from the previous step)

References:
* [Using StatefulSets to setup Postgres](https://github.com/arianitu/postgres-statefulset)