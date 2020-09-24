# k8s-java-warm-up
monitor java web appilication warm up effect in k8s

## 环境变量



## 获得 token

[文档]( https://yq.aliyun.com/articles/672460)

步骤


### 1. 得到 secret

`kubectl get sa admin -n kube-system -o yaml`

```
[root@iZwz94wwgi0sw7jukkbfepZ ~]# kubectl get sa admin -n kube-system -o yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  annotations:
    kubectl.kubernetes.io/last-applied-configuration: |
      {"apiVersion":"v1","kind":"ServiceAccount","metadata":{"annotations":{},"name":"admin","namespace":"kube-system"}}
  creationTimestamp: "2019-04-16T07:29:09Z"
  name: admin
  namespace: kube-system
  resourceVersion: "1344"
  selfLink: /api/v1/namespaces/kube-system/serviceaccounts/admin
  uid: 53087d4b-6019-11e9-b5e4-00163e024bf5
secrets:
- name: admin-token-9jszw
```

secret 是 `admin-token-9jszw`

### 2. 得到 base64 编码后的 token

``` shell script
kubectl get secret {secret} -n kube-system -o jsonpath={".data.token"} | base64 -d
```