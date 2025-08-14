#!/bin/zsh
echo "🏗️  Building production JAR..."

# Clean and build with production profile
./mvnw clean package -DskipTests -Pprod

echo "✅ JAR created at: target/backend-0.0.1-SNAPSHOT.jar"
echo "📦 Ready for deployment to AWS EC2!"
echo "📊 JAR size: $(du -h target/backend-0.0.1-SNAPSHOT.jar | cut -f1)"