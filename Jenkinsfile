#!groovy


def upgradeRelease(Map<String, String> overwrites) {
    def arguments = ""

    for (ow in overwrites) {
        arguments += "${ow.key}=${ow.value},"
    }

    if (arguments.length() > 0) {
        arguments = " --set " + arguments
        arguments = arguments.substring(0, arguments.length() - 1)
    }
    sh "helm repo add ${HELM_CHART_REGISTRY_NAME} ${HELM_CHART_REGISTRY_URL}"
    sh "helm repo update"
    //sh "./findfailed.sh ${NAMESPACE} ${DOCKER_IMAGE}"
    sh "helm upgrade --wait --install --atomic --timeout 500s --namespace=${NAMESPACE} ${DOCKER_IMAGE} ${HELM_CHART_REGISTRY_NAME}/${HELM_CHART_NAME} --version ${params.helmChartVersion}  -f ./k8s/values-${params.envName}.yaml --debug" + arguments
}

def suppressEcho(cmd) {
    steps.sh(script: '#!/bin/sh -e\n' + cmd, returnStdout: true)
}

def getKubeconfig() {
    sh "mkdir -p /root/.kube && touch /root/.kube/config"
    def secretsFile = "./k8s/rancher-${params.envName}.json"
    echo "Fetching rancher url"
    def rancherURL = suppressEcho("cat ${secretsFile} | jq '.URL' -r").trim()
    echo "Fetching bot token"
    def botToken = suppressEcho("cat ${secretsFile} | jq '.token' -r").trim()
    echo "Fetching clusterId"
    def clusterId = suppressEcho("curl -H 'Authorization: Bearer ${botToken}' ${rancherURL}/v3/clusters?name=${params.clusterName} | jq '.data[0].id' -r").trim()
    echo "Fetching cluster kubeconfig"
    suppressEcho("curl -u \"${botToken}\" -X POST -H 'Accept: application/yaml' -H 'Content-Type: application/yaml' -d '{}' '${rancherURL}/v3/clusters/${clusterId}?action=generateKubeconfig' | awk '/config:/{f=1;next} /type:/{f=0} f' >> /root/.kube/config")
}

pipeline {
    agent {
        label 'generic'     // Execute the Pipeline, or stage, on an agent available in the Jenkins environment with the provided label
    }

    environment {     // The environment block has a helper method credentials() defined which can be used to access pre-defined Credentials by their identifier in the Jenkins environment.

        NAMESPACE = "god-zadro" // change , bilo god-zadro
        TEAM_NAME = "god"  //svn, int, cas, ngs bilo god

        DOCKER_REGISTRY = "docker-app.nsoft.com:10884" // 10882-seven 10886-casino 10887-integrations 
        DOCKER_REGISTRY_CREDENTIALS_ID = "docker-bot-test-onboarding" //change (provided via LP ... jenkins.bot.xxx and neeeds to be stored in jenkins credentials)
        DOCKER_IMAGE = "onboardingtutorial" //change (project name or something)
        JENKINS_VARIJABLA = "Vrijednost"
        JENKINS_VARIJABLA1 = "Vrijednost2"
        TRANSCRYPT_CREDENTIALS_ID = "transcrypt-onboardingtutorial" //change

        DOCKER_GOD_REGISTRY = "docker-app.nsoft.com:10884"
        DOCKER_GOD_REGISTRY_CREDENTIALS_ID = "god-readonly-global"
        HELM_IMAGE = "helm:3.0.0-phoenix1"
        // HELM_IMAGE = "helm:3.8.1-phoenix0"

        HELM_CHART_REGISTRY_URL = "https://chartmuseum.utility.nsoft.cloud/"
        HELM_CHART_REGISTRY_NAME = "chartmuseum"
        HELM_CHART_NAME = "nsoft-helm-template-chart"
    }
    stages {

        stage('Decrypt files') {        // it is recommended that stages contain at least one stage directive for each discrete part of the continuous delivery process, such as Build, Test, and Deploy.
            steps {                     // The steps section defines a series of one or more steps to be executed in a given stage directive.


                withCredentials([
                        file(credentialsId: "${TRANSCRYPT_CREDENTIALS_ID}", variable: 'TR_PASS')
                ]) {
                    suppressEcho("./transcrypt -c aes-256-cbc -p \$(cat ${TR_PASS}) --yes && ./transcrypt --list")
                    stash name: "decrypted-repo", includes: "**"
                }
            }
        }


        stage('Build & Publish Docker') {

            steps {
                unstash "decrypted-repo"
                withDockerRegistry([credentialsId: "${DOCKER_REGISTRY_CREDENTIALS_ID}", url: "https://${DOCKER_REGISTRY}"]) {
                    sh "docker build --no-cache -t ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${GIT_COMMIT} ."
                    sh "docker push ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${GIT_COMMIT}"
                }
            }
        }

        stage("Deploy") {
            agent {
                docker {
                    image "${DOCKER_GOD_REGISTRY}/${HELM_IMAGE}"
                    args "-u root"                                                 // A string. Runtime arguments to pass to docker run.
                    registryUrl "https://${DOCKER_GOD_REGISTRY}"
                    registryCredentialsId "${DOCKER_GOD_REGISTRY_CREDENTIALS_ID}"    // docker also optionally accepts a registryUrl and registryCredentialsId parameters which will help to specify the Docker Registry to use and its credentials
                    label "generic"
                }
            }
            steps {
                unstash "decrypted-repo"
                getKubeconfig()
                withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: "${DOCKER_REGISTRY_CREDENTIALS_ID}", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                    upgradeRelease([
                            "namespace"                : NAMESPACE,
                            "teamName"                 : TEAM_NAME,
                            "image.appVersion"         : GIT_COMMIT,
                            "cluster"                  : params.clusterName,
                            "image.name"               : DOCKER_IMAGE,
                            "imageCredentials.username": USERNAME,
                            "imageCredentials.password": PASSWORD,
                            "imageCredentials.registry": DOCKER_REGISTRY,
                            "envSecret.TEST1"          : JENKINS_VARIJABLA,
                            "envSecret.TEST2"          : JENKINS_VARIJABLA1


                    ])
                }
                deleteDir()
            }
            post {                                // The post section defines one or more additional steps that are run upon the completion of a Pipeline’s or stage’s run
                failure {
                    sh "helm rollback ${DOCKER_IMAGE} 0 --namespace ${NAMESPACE}"
                }
            }
        }

    }
    post {
        always {
            cleanWs()
        }
    }
}



// post {
//     failure {
//         sh "helm rollback ${DOCKER_IMAGE} 0 --namespace ${NAMESPACE}"
//     }
//     aborted {
//         sh "helm rollback ${DOCKER_IMAGE} 0 --namespace ${NAMESPACE}"
//     }
    
//     }
// }

// post {
//         always {
//             cleanWs()
//         }
//     }
// }
