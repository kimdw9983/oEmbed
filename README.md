# oEmbed
Lightweight, Restful OEmbed api server implementation.

## What does this do?
- Implementation of [OEmbed](https://oembed.com/).
- Basic url validation. Raises correspond errors if any(e.g MalformedURL, No content from URL..)
- Automatically finds OEmbed data from given url.
- Using up-to-date [providers.json](https://oembed.com/providers.json).

## How to use?
- Build and run this api server first. And it will respond to basic HTTP requests.

```
GET {api_server_url}:8080/oembed/{url}
```

### Example
if you have run server on your local in Spring Boot, you can simply test it on CMD.
```
curl localhost:8080/oembed/https://youtu.be/FtutLA63Cp8
```

or Postman
![image](https://user-images.githubusercontent.com/93891414/181404845-7998f4bd-fb56-42a1-a6f5-bab0c419d97d.png)

and respond will be like this:
```
{"status":"OK","data":{"title":"【東方】Bad Apple!! ＰＶ【影絵】","author_name":"kasidid2","author_url":"https://www.youtube.com/user/kasidid2","type":"video","height":150,"width":200,"version":"1.0","provider_name":"YouTube","provider_url":"https://www.youtube.com/","thumbnail_height":360,"thumbnail_width":480,"thumbnail_url":"https://i.ytimg.com/vi/FtutLA63Cp8/hqdefault.jpg","html":"<iframe width=\"200\" height=\"150\" src=\"https://www.youtube.com/embed/FtutLA63Cp8?feature=oembed\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen title=\"【東方】Bad Apple!! ＰＶ【影絵】\"></iframe>"}}
```
