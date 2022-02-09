// https://www.jenkins.io/doc/book/pipeline/syntax/#agent-parameters
// https://www.jenkins.io/doc/book/pipeline/jenkinsfile/
// http://groovy-lang.org/syntax.html

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
    sh "helm upgrade --wait --install --atomic --timeout 500s --namespace=${NAMESPACE} ${DOCKER_IMAGE} ${HELM_CHART_REGISTRY_NAME}/${HELM_CHART_NAME} --version ${params.helmChartVersion}  -f ./k8s/values-${params.envName}.yaml --debug" + arguments
}

def suppressEcho(cmd) {
    steps.sh(script: '#!/bin/sh -e\n' + cmd, returnStdout: true)
}

def getKubeconfig() {
    sh "mkdir -p /root/.kube && touch /root/.kube/config"
    def secretsFile = "./k8s/rancher-${params.envName}.json" //The parameters directive provides a list of parameters that a user should provide when triggering the Pipeline. The values for these user-specified parameters are made available to Pipeline steps via the params object
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
        label 'generic'
    }

    environment { //The environment directive specifies a sequence of key-value pairs which will be defined as environment variables for all steps, or stage-specific steps, depending on where the environment directive is located within the Pipeline.



        NAMESPACE = "god-zadro" // change , bilo god-zadro
        TEAM_NAME = "god"  //svn, int, cas, ngs bilo god

        DOCKER_REGISTRY = "docker-app.nsoft.com:10884" // 10882-seven 10886-casino 10887-integrations 
        DOCKER_REGISTRY_CREDENTIALS_ID = "docker-bot-test-onboarding" //change (provided via LP ... jenkins.bot.xxx and neeeds to be stored in jenkins credentials)
        DOCKER_IMAGE = "onboardingtutorial" //change (project name or something)

        TRANSCRYPT_CREDENTIALS_ID = "transcrypt-onboardingtutorial" //change

        DOCKER_GOD_REGISTRY = "docker-app.nsoft.com:10884"
        DOCKER_GOD_REGISTRY_CREDENTIALS_ID = "god-readonly-global"
        HELM_IMAGE = "helm:3.0.0-phoenix1"

        HELM_CHART_REGISTRY_URL = "https://chartmuseum.utility.nsoft.cloud/"
        HELM_CHART_REGISTRY_NAME = "chartmuseum"
        HELM_CHART_NAME = "nsoft-helm-template-chart"
    }
    stages { //Containing a sequence of one or more stage directives, the stages section is where the bulk of the "work" described by a Pipeline will be located

        stage('Decrypt files') {  //The steps section defines a series of one or more steps to be executed in a given stage directive.


            steps {
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
                docker { //Execute the Pipeline, or stage, with the given container which will be dynamically provisioned on a node pre-configured to accept Docker-based Pipelines,
                    image "${DOCKER_GOD_REGISTRY}/${HELM_IMAGE}"
                    args "-u root"   //args parameter which may contain arguments to pass directly to a docker run invocation. A string. Runtime arguments to pass to docker run.
                    registryUrl "https://${DOCKER_GOD_REGISTRY}" //registryUrl and registryCredentialsId parameters which will help to specify the Docker Registry to use and its credentials
                    registryCredentialsId "${DOCKER_GOD_REGISTRY_CREDENTIALS_ID}" //The parameter registryCredentialsId could be used alone for private repositories within the docker hub
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
                    ])
                }
                deleteDir()
            }
            post {
                failure { //Only run the steps in post if the current Pipeline’s or stage’s run has a "failed" status, typically denoted by red in the web UI.


                    sh "helm rollback ${DOCKER_IMAGE} 0 --namespace ${NAMESPACE}"
                }
            }
        }

    }
    post { //The post section defines one or more additional steps that are run upon the completion of a Pipeline’s or stage’s run
        always {   //Run the steps in the post section regardless of the completion status of the Pipeline’s or stage’s run.


            cleanWs()
        }
    }
}