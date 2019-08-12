#### Instructions (for Minikube):

* `minikube start -p kotlink --memory 16gb --cpus 4 --disk-size 64gb`
* `minikube -p kotlink addons enable metrics-server`
* `minikube -p kotlink addons enable ingress`
* `kubectx kotlink`

* `kubectl apply -f namespace.yaml`
* `kubens kotlink`

* `kubectl apply -f postgres-secret.yaml`
* `kubectl apply -f postgres-statefulset.yaml`
* `kubectl apply -f postgres-service.yaml`

* `kubectl apply -f redis-deployment.yaml`
* `kubectl apply -f redis-service.yaml`

* `kubectl apply -f kotlink-secret.yaml`
* `kubectl apply -f kotlink-deployment.yaml`
* `kubectl apply -f kotlink-service.yaml`
* `kubectl apply -f kotlink-ingress.yaml`

* Run `minikube ip` and add the resulting IP into `/etc/hosts` 
(e.g. `192.168.99.100 local.kotlink.org`)
* Open [local.kotlink.org](https://local.kotlink.org)

To access Redis:
* Run `kubectl port-forward service/redis 6379:6379` in one Terminal
* In a different terminal run `redis-cli`

To access Postgres:
* Run `kubectl port-forward service/postgres 5432:5432` in one Terminal
* In a different terminal run `psql -h 127.0.0.1 -p 5432 -U kotlinkuser -W kotlink` (password is `kotlinkpass`)

TODOs:
* Add limits to cpu/memory
* [Deploying Redis Cluster on Top of Kubernetes](https://rancher.com/blog/2019/deploying-redis-cluster/)

References:
* [Using StatefulSets to setup Postgres](https://github.com/arianitu/postgres-statefulset)