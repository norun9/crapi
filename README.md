
Create new in post table

request

`curl -X POST -H "Content-Type:application/json" -d '{"user_id":"11111111-1111-1111-1111-111111111111","text":"hello world"}' localhost:9000/posts/create`

response

`{"result":"OK"}`

|Id|User_id|Text|Comment_count|Posted_at|
|--|-------|----|-------------|---------|
|17ff0e30-050a-4af3-b803-af4fe3b0d380|11111111-1111-1111-1111-111111111111|hello world|0|2020-08-24 22:33:51.580447|



