{\rtf1\ansi\ansicpg949\cocoartf2708
\cocoatextscaling0\cocoaplatform0{\fonttbl\f0\fswiss\fcharset0 Helvetica;\f1\fnil\fcharset129 AppleSDGothicNeo-Regular;}
{\colortbl;\red255\green255\blue255;\red0\green0\blue0;}
{\*\expandedcolortbl;;\cssrgb\c0\c0\c0;}
\paperw11900\paperh16840\margl1440\margr1440\vieww11520\viewh8400\viewkind0
\deftab720
\pard\pardeftab720\partightenfactor0

\f0\fs24 \cf2 \expnd0\expndtw0\kerning0
#!/bin/bash\
#PROJECT_NAME="github_action"\
JAR_PATH="/home/ubuntu/github_action/build/libs/*.jar"\
DEPLOY_PATH=/home/ubuntu/$PROJECT_NAME/\
DEPLOY_LOG_PATH="/home/ubuntu/github_action/deploy.log"\
DEPLOY_ERR_LOG_PATH="/home/ubuntu/github_action/deploy_err.log"\
APPLICATION_LOG_PATH="/home/ubuntu/github_action/application.log"\
BUILD_JAR=$(ls $JAR_PATH)\
JAR_NAME=$(basename $BUILD_JAR)\
\uc0\u8232 \
echo "===== 
\f1 \'b9\'e8\'c6\'f7
\f0  
\f1 \'bd\'c3\'c0\'db
\f0  : $(date +%c) =====" >> $DEPLOY_LOG_PATH\
\uc0\u8232 \
echo "> build 
\f1 \'c6\'c4\'c0\'cf\'b8\'ed
\f0 : $JAR_NAME" >> $DEPLOY_LOG_PATH\
echo "> build 
\f1 \'c6\'c4\'c0\'cf
\f0  
\f1 \'ba\'b9\'bb\'e7
\f0 " >> $DEPLOY_LOG_PATH\
cp $BUILD_JAR $DEPLOY_PATH\
\uc0\u8232 \
echo "> 
\f1 \'c7\'f6\'c0\'e7
\f0  
\f1 \'b5\'bf\'c0\'db\'c1\'df\'c0\'ce
\f0  
\f1 \'be\'ee\'c7\'c3\'b8\'ae\'c4\'c9\'c0\'cc\'bc\'c7
\f0  pid 
\f1 \'c3\'bc\'c5\'a9
\f0 " >> $DEPLOY_LOG_PATH\
CURRENT_PID=$(pgrep -f $JAR_NAME)\
\uc0\u8232 \
if [ -z $CURRENT_PID ]\
then\
  echo "> 
\f1 \'c7\'f6\'c0\'e7
\f0  
\f1 \'b5\'bf\'c0\'db\'c1\'df\'c0\'ce
\f0  
\f1 \'be\'ee\'c7\'c3\'b8\'ae\'c4\'c9\'c0\'cc\'bc\'c7
\f0  
\f1 \'c1\'b8\'c0\'e7
\f0  X" >> $DEPLOY_LOG_PATH\
else\
  echo "> 
\f1 \'c7\'f6\'c0\'e7
\f0  
\f1 \'b5\'bf\'c0\'db\'c1\'df\'c0\'ce
\f0  
\f1 \'be\'ee\'c7\'c3\'b8\'ae\'c4\'c9\'c0\'cc\'bc\'c7
\f0  
\f1 \'c1\'b8\'c0\'e7
\f0  O" >> $DEPLOY_LOG_PATH\
  echo "> 
\f1 \'c7\'f6\'c0\'e7
\f0  
\f1 \'b5\'bf\'c0\'db\'c1\'df\'c0\'ce
\f0  
\f1 \'be\'ee\'c7\'c3\'b8\'ae\'c4\'c9\'c0\'cc\'bc\'c7
\f0  
\f1 \'b0\'ad\'c1\'a6
\f0  
\f1 \'c1\'be\'b7\'e1
\f0  
\f1 \'c1\'f8\'c7\'e0
\f0 " >> $DEPLOY_LOG_PATH\
  echo "> kill -9 $CURRENT_PID" >> $DEPLOY_LOG_PATH\
  kill -9 $CURRENT_PID\
fi\
\uc0\u8232 \
DEPLOY_JAR=$DEPLOY_PATH$JAR_NAME\
echo "> DEPLOY_JAR 
\f1 \'b9\'e8\'c6\'f7
\f0 " >> $DEPLOY_LOG_PATH\
nohup java -jar $DEPLOY_JAR >> $APPLICATION_LOG_PATH 2> $DEPLOY_ERR_LOG_PATH &\
\uc0\u8232 \
sleep 3\
\uc0\u8232 \
echo "> 
\f1 \'b9\'e8\'c6\'f7
\f0  
\f1 \'c1\'be\'b7\'e1
\f0  : $(date +%c)" >> $DEPLOY_LOG_PATH\
}