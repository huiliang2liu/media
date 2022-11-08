#./gradlew arm64:assembleDebug
#./gradlew arm64:publishPluginPublicationToNexusRepository
#./gradlew armeabi:assembleDebug
#./gradlew armeabi:publishPluginPublicationToNexusRepository
#./gradlew armeabi_v7:assembleDebug
#./gradlew armeabi_v7:publishPluginPublicationToNexusRepository
./gradlew media:assembleDebug
./gradlew media:publishPluginPublicationToNexusRepository
#./gradlew x86:assembleDebug
#./gradlew x86:publishPluginPublicationToNexusRepository
p=`pwd`
cd /Users/liuhuiliang/work/gitMaven
echo ".DS_Store" > .gitignore
git add .
git commit -m 这是测试
git push origin master
cd $p