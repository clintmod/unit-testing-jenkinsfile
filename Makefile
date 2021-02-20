

test-jenkinsfile:
	docker run --rm -v $(PWD):/home/groovy/app groovy:3.0.6 \
			bash -c "cd /home/groovy/app && \
			groovy -cp scripts/jenkinsfile scripts/jenkinsfile/Tests.groovy"