from youtube_transcript_api import YouTubeTranscriptApi
import json

import os
import sys
sep = '&&&'
json_url = os.getcwd()  # 'your_server_path'

video_url = sys.argv[1]

video_id = video_url.split('v=')[1]
ampersandPosition = video_id.find('&')
if ampersandPosition != -1:
    video_id = video_id[0: ampersandPosition]

caption = YouTubeTranscriptApi.get_transcript(video_id)
caption_dict = {item['text']: item['start'] for item in caption}

for k, v in caption_dict.items():
    print('%s%s%s' % (k.lower(), sep, v))

