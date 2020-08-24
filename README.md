
Create new in post table

request

`curl -X POST -H "Content-Type:application/json" -d '{"user_id":"11111111-1111-1111-1111-111111111111","text":"hello world"}' localhost:9000/posts/create`

response

`{"result":"OK"}`

|Id|User_id|Text|Comment_count|Posted_at|
|--|-------|----|-------------|---------|
|17ff0e30-050a-4af3-b803-af4fe3b0d380|11111111-1111-1111-1111-111111111111|hello world|0|2020-08-24 22:33:51.580447|


post index

request

`curl -XGET http://localhost:9000/posts`

response

`{"posts":[[{"id":"17ff0e30-050a-4af3-b803-af4fe3b0d380","user_id":"11111111-1111-1111-1111-111111111111","text":"hello world","comment_count":0,"posted_at":"2020-08-24T22:33:51.58"},[],[]]]}`

comment create

request

`curl -X POST -H "Content-Type:application/json" -d '{"user_id": "11111111-1111-1111-1111-111111111111", "text": "Comment"}' localhost:9000/posts/17ff0e30-050a-4af3-b803-af4fe3b0d380/comments/create`

response

`{"result":"OK"}`

|Id|User_id|Text|Parent_post_id|Comment_count|Posted_at|
|--|-------|----|--------------|-------------|---------|
|a4203cb4-966d-46bc-9d2d-5309d238f20e|11111111-1111-1111-1111-111111111111|Comment|17ff0e30-050a-4af3-b803-af4fe3b0d380|0|2020-08-24 22:33:51.580447|

(At this time, the comment_count of the post which id is a4203cb4-966d-46bc-9d2d-5309d238f20e is incremented by 1)

Comment index

request

`curl -X GET http://localhost:9000/posts/17ff0e30-050a-4af3-b803-af4fe3b0d380/comments`

response

`{"comments":[{"id":"a4203cb4-966d-46bc-9d2d-5309d238f20e","user_id":"11111111-1111-1111-1111-111111111111","text":"Comment","parent_post_id":"17ff0e30-050a-4af3-b803-af4fe3b0d380","comment_count":0,"posted_at":"2020-08-24T22:36:27.556"}]}`

Create a comment for optional comment

request

`curl -X POST -H "Content-Type:application/json" -d '{"user_id": "11111111-1111-1111-1111-111111111111", "text": "nest comment"}' localhost:9000/posts/a4203cb4-966d-46bc-9d2d-5309d238f20e/comments/create`

|Id|User_id|Text|Parent_post_id|Comment_count|Posted_at|
|--|-------|----|--------------|-------------|---------|
|7b4ec80d-6bab-4d3f-8014-bfcce3d21961|11111111-1111-1111-1111-111111111111|nest comment|17ff0e30-050a-4af3-b803-af4fe3b0d380|0|2020-08-24 22:38:59.027121|

(At this time, the comment_count of the comment which id is 17ff0e30-050a-4af3-b803-af4fe3b0d380 is incremented by 1)
