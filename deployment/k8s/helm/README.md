#### Instructions

* `minikube start --memory 16gb --cpus 8 --disk-size 64gb --vm-driver kvm2`
* `minikube addons enable metrics-server`
* `minikube addons enable ingress`
* `minikube addons enable dashboard`
* `kubectx minikube`

* `kubectl create namespace kotlink`
* `kubens kotlink`

* `helm init --history-max 200`

* `helm install kotlink --name kotlink`