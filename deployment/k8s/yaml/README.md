#### Instructions (for Minikube):

* `minikube addons enable ingress`
* `kubectl apply -f namespace.yaml`
* `kubens kotlink`
* `kubect apply -f postgres-secret.yaml`
* `kubect apply -f postgres-statefulset.yaml`
* `kubect apply -f postgres-service.yaml`
* `kubect apply -f kotlink-secret.yaml`
* `kubect apply -f kotlink-deployment.yaml`
* `kubect apply -f kotlink-service.yaml`
* `kubect apply -f kotlink-ingress.yaml`
* Run `minikube ip` and add the resulting IP into `/etc/hosts` 
(e.g. `192.168.99.100 local.kotlink.org`)
* Open [local.kotlink.org](https://local.kotlink.org)

To access Postgres:
* Run `kubectl port-forward service/postgres 5432:5432` in one Terminal
* In a different terminal run `psql -h 127.0.0.1 -p 5432 -U kotlinkuser -W kotlink` (password is `kotlinkpass`)