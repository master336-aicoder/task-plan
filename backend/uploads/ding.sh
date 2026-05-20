dingtext="【bj-nas】"$1
echo $dingtext
curl https://oapi.dingtalk.com/robot/send?access_token=de52f3ae7375de6d2acf086bb90a4ef7b88851b7a3c436cb30b4534f7010e7cd -H "Content-Type:application/json"  -X POST -d "{\"msgtype\":\"text\",\"text\":{\"content\":\"$dingtext\"}}"
