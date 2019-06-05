import colorsys
import os
import random
import time
import os
import numpy as np
from PIL import Image, ImageDraw, ImageFont, ImageEnhance
import json
from pprint import pprint
import cv2
import sys
import numpy as np 
import csv

def inrangeof(nu, frames, tracks):
	if str(float(nu)//15) in tracks:
		return True
	return False

def ff(nu, frames, tracks ):
	i = 0.0
	for key in tracks:
		if float(key)*15 <= float(nu) and float(nu) <= float(key)*15 +15:
			return key 

def labeling_car(vidInput):
	with open('car_track_j_'+vidInput+'.json') as f:
		tracks = json.load(f)
	divlen = len(tracks)
	vid = vidInput+"_2.MP4"
	fourcc = cv2.VideoWriter_fourcc(*'XVID')
	capture = cv2.VideoCapture(vid)
	fps = int(capture.get(cv2.CAP_PROP_FPS))
	out1 =cv2.VideoWriter()
	size = (int(capture.get(cv2.CAP_PROP_FRAME_WIDTH)),int(capture.get(cv2.CAP_PROP_FRAME_HEIGHT)))
	total = int(capture.get(cv2.CAP_PROP_FRAME_COUNT)) - 2
	frames_done = 0
	font = cv2.FONT_HERSHEY_SIMPLEX
	V1 = 'RV_' + vid
	V1 = os.path.splitext(V1)[0]+'.avi'
	out1.open(V1, fourcc, fps, size, 1)

	while (total > 0):
		total = total - 1
		stime = time.time()
		ret, frame = capture.read()

		if not ret:
			capture.set(cv2.CAP_PROP_POS_FRAMES, pos_frame+1)
			print("Error!", pos_frame+1)
			cv2.waitKey(10)

		if ret:
			pos_frame = capture.get(cv2.CAP_PROP_POS_FRAMES)
			frames_done = frames_done + 1

			for pednum in tracks:
				print ("pednum", pednum)
				if str(pos_frame) in tracks[pednum]:
					print ("frame", pos_frame)
					for frame_des in tracks[pednum][str(pos_frame)]:
						X = tracks[pednum][str(pos_frame)]["cord"]["X"]
						Y = tracks[pednum][str(pos_frame)]["cord"]["Y"]
						colo = (int(255*int(pednum)/divlen),255/(int(pednum)+1),255-int(255*int(pednum)/divlen))
						cv2.putText(frame,pednum,(int(Y),int(X)), font, 2,colo,3,cv2.LINE_AA)
						bottom = int(X)
						left = int(Y)
						top = int(tracks[pednum][str(pos_frame)]["cord"]["top"])
						right = int(tracks[pednum][str(pos_frame)]["cord"]["right"])
						pts = np.array([[left,top],[left,bottom],[right,bottom],[right,top]], np.int32)
						cv2.polylines(frame, [pts], True, colo, 3)
						pts = np.array([[1350,370],[1100,480],[1400,580],[1500,480]], np.int32)
						cv2.polylines(frame, [pts], True, colo, 3)
		out1.write(frame)

def labeling_ped(vidInput):
	with open('ped_track_j_'+vidInput+'.json') as f:
		tracks = json.load(f)
	divlen = len(tracks)
	vid = "RV_"+vidInput+"_2.avi"
	fourcc = cv2.VideoWriter_fourcc(*'XVID')
	capture = cv2.VideoCapture(vid)
	fps = int(capture.get(cv2.CAP_PROP_FPS))
	out1 =cv2.VideoWriter()
	size = (int(capture.get(cv2.CAP_PROP_FRAME_WIDTH)),int(capture.get(cv2.CAP_PROP_FRAME_HEIGHT)))
	total = int(capture.get(cv2.CAP_PROP_FRAME_COUNT)) - 2
	frames_done = 0
	font = cv2.FONT_HERSHEY_SIMPLEX
	V1 = 'RP_' + vid
	V1 = os.path.splitext(V1)[0]+'.avi'
	out1.open(V1, fourcc, fps, size, 1)

	while (total > 0):
		total = total - 1
		stime = time.time()
		ret, frame = capture.read()

		if not ret:
			capture.set(cv2.CAP_PROP_POS_FRAMES, pos_frame+1)
			print("Error!", pos_frame+1)
			cv2.waitKey(10)

		if ret:
			pos_frame = capture.get(cv2.CAP_PROP_POS_FRAMES)
			frames_done = frames_done + 1

			for pednum in tracks:
				print ("pednum", pednum)
				if str(pos_frame) in tracks[pednum]:
					print ("frame", pos_frame)
					for frame_des in tracks[pednum][str(pos_frame)]:
						X = tracks[pednum][str(pos_frame)]["cord"]["X"]
						Y = tracks[pednum][str(pos_frame)]["cord"]["Y"]
						colo = (int(255*int(pednum)/divlen),255/(int(pednum)+1),255-int(255*int(pednum)/divlen))
						cv2.putText(frame,pednum,(int(Y),int(X)), font, 2,colo,3,cv2.LINE_AA)
						bottom = int(X)
						left = int(Y)
						pts = np.array([[left,bottom-10],[left,bottom],[left+10,bottom],[left+10,bottom-10]], np.int32)
						cv2.polylines(frame, [pts], True, colo, 3)
		out1.write(frame)
	os.remove("RV_"+vidInput+"_2.avi")

if __name__ == '__main__':
#	labeling_car("GH010228")
#	labeling_ped("GH010228")
#	labeling_car("GH020228")
#	labeling_ped("GH020228")
#	labeling_car("GH030228")
#	labeling_ped("GH030228")
#	labeling_car("GH040228")
#	labeling_ped("GH040228")
#	labeling_car("GH050228")
#	labeling_ped("GH050228")
#	labeling_car("GH060228")
#	labeling_ped("GH060228")
#	labeling_car("GH070228")
#	labeling_ped("GH070228")
	labeling_car("GH080228")
	labeling_ped("GH080228")
	labeling_car("GH090228")
	labeling_ped("GH090228")
	labeling_car("GH100228")
	labeling_ped("GH100228")