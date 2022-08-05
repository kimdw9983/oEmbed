# oEmbed
Lightweight, Restful oEmbed api server implementation.

## What does this do?
- Implementation of [oEmbed](https://oembed.com/).
- Basic url validation. It can handle url only with `http` and `https` protocol.
- Raises correspond errors to response via Http status code with messages, if any. \
` e.g.) BAD_REQUEST(MalformedURL, not valid URI), NOT_IMPLEMENTED(No content from URL, no providers found)`
- Automatically finds oEmbed data from given url, by parsing provider_url and url schemes.
- Using up-to-date [providers.json](https://oembed.com/providers.json).

## How to use?
- Import project as Gradle.
- Build and run this api server and it will respond to basic HTTP requests.
- Of course it should be able to connect to those providers in list -> https://oembed.com/providers.json

### Usage
```
GET {api_server_url}:8080/oembed/{url}
```

### Example
if you have run server on your local in Spring Boot, you can simply test it on CMD.
```
curl localhost:8080/oembed/https://youtu.be/FtutLA63Cp8
```

or Postman
![image](https://user-images.githubusercontent.com/93891414/181448587-a1fbe099-31e9-4455-8493-99d9bb4828e0.png)

then respond (body) will be like this:
```
{
  "status": "OK",
  "data": {
    "title": "【東方】Bad Apple!! ＰＶ【影絵】",
    "author_name": "kasidid2",
    "author_url": "https://www.youtube.com/user/kasidid2",
    "type": "video",
    "height": 150,
    "width": 200,
    "version": "1.0",
    "provider_name": "YouTube",
    "provider_url": "https://www.youtube.com/",
    "thumbnail_height": 360,
    "thumbnail_width": 480,
    "thumbnail_url": "https://i.ytimg.com/vi/FtutLA63Cp8/hqdefault.jpg",
    "html": "<iframe width=\"200\" height=\"150\" src=\"https://www.youtube.com/embed/FtutLA63Cp8?feature=oembed\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen title=\"【東方】Bad Apple!! ＰＶ【影絵】\"></iframe>"
  }
}
```

## Compatibility
- Tested on <b>Java 8</b>. And it should work on higher version.

## TODO & ISSUE(sort of)
- [Instagram and some facebook content currently require credentials](https://developers.facebook.com/docs/instagram/oembed/) for responding oEmbed, supporting it.
- Adding filter options(for better performance), including or excluding certain providers.
