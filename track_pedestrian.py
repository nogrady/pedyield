#! /usr/bin/env python
# -*- coding: utf-8 -*-
import csv
import numpy as np 
import cv2
import random
import sys
import os
import json
from PIL import Image, ImageDraw, ImageFont
# video dependent
fps = 60
# distance between the centroids for filtering duplicate cars
filter_dis = 10
# TTL seconds
max_dist_per_sec = 5
max_time_wait = 5
# TTL frames
min_length = fps*max_time_wait

def distance(x1,y1,x2,y2):
	return (np.sqrt( (x2 - x1)**2 + (y2 - y1)**2 ))

def convort2cv(img):
	pil_image = img.convert('RGB')
	open_cv_image = np.array(pil_image)
	open_cv_image = open_cv_image[:,:, ::-1].copy()
	return open_cv_image

def calcentroid(pers):
	x = (pers[2]+pers[4])/2
	y = (pers[3] + pers[5])/2
	return x,y

def filterhelp(fil_pers):
	fil_return = []
	fil_return_copy =[]
	to_Del = []
	backup_list = {}
	check = True
	id = -1
	while(check):
		id = id + 1
		fil_new = []
		found_same = False
		fil_return = fil_pers[:]
		backup_list[id] = []
		for i in range(len(fil_pers)):
			fil_return_copy = []
			to_Del =[]
			pers_same = []
			for inum in range(len(fil_pers[i])):
				pers_same.append(fil_pers[i][inum])
			for j in range(i+1, len(fil_return)):
				if (j > len(fil_return) -1):
					break 
				for k in range(len(fil_return[j])):
					if (fil_return[j][k] in pers_same):
						for t in range(len(fil_return[j])):
							if (t != k):
								if not(fil_return[j][t] in pers_same):
									pers_same.append(fil_return[j][t])
						if j not in to_Del:
							to_Del.append(j)
						found_same = True
			for elem in range(len(fil_return)):
				if elem not in to_Del:
					fil_return_copy.append(fil_return[elem])
			backup_list[id] = fil_return_copy[:]
			if id != 0 and len(backup_list[id]) == len(backup_list[id-1]):
				return backup_list[id]
				check = False
				break
			fil_return = fil_return_copy[:]
			fil_new.append(pers_same)
		fil_pers = []
		fil_pers = fil_new[:]
		fil_new = []
		if not found_same:
			break
	return fil_pers

def filterpers(pers_frame):
	fil_pers = []
	unique_pers = []
	for i in range(len(pers_frame)):
		persnow = pers_frame[i]
		for j in range(i+1, len(pers_frame)):
			persnew = pers_frame[j]
			dis = distance(persnow[4], persnow[5], persnew[4], persnew[5])
			if dis < filter_dis:
				dd = [i,j]
				print (dd)
				fil_pers.append(dd)
	fil_pers = filterhelp(fil_pers)
	fil_per_done = []
	for i in range(len(pers_frame)):
		if i not in fil_per_done:
			throw = True
			for j in range(len(fil_pers)):
				if i in fil_pers[j]:
					up = []
					for k in fil_pers[j]:
						up.append(pers_frame[k])
						fil_per_done.append(k)
					mean_per = np.array([up]).mean(axis=0).tolist()
					print (mean_per)
					unique_pers.append(mean_per[0])
					throw = False
					break
			if throw:
				unique_pers.append(pers_frame[i])
	return unique_pers

def samepers(pers_frame, pers, final_dict):
	per_done = []
	for j in range(len(pers_frame)):
		min_dist = 1000000000000
		pers_id = -1
		diff_frames_store = -1
		for i in range(len(pers)):
			if i not in per_done:
				last_frame = pers[i][-1]
				dis = distance(last_frame[4],last_frame[5], pers_frame[j][4],pers_frame[j][5])
				diff_frames = abs(pers_frame[j][0] - last_frame[0])
				if pers_frame[j][0] > 100:
					print (pers_frame[j])
				if dis < min_dist and dis < max_dist_per_sec*(diff_frames) and abs(diff_frames) < fps*max_time_wait:
					if i==1 and last_frame[0] >17:
						print("got it", dis)
					min_dist = dis
					pers_id = i
					diff_frames_store = abs(diff_frames)
		if pers_id == -1:
			print("min_dist  ", min_dist)
			print("pers_id  ",pers_id)
		per_done.append(pers_id)
		if pers_id != -1:
			print(diff_frames_store)
			for it in range(1, int(diff_frames_store)+1):
				print (it)
				pers_append = [pers[pers_id][-1][0] + 1, it*(float(pers_frame[j][1])/diff_frames_store), 
								float(pers[pers_id][-1][2]) - it*(float((pers[pers_id][-1][2] - pers_frame[j][2])/float(diff_frames_store))),
								float(pers[pers_id][-1][3]) - it*(float((pers[pers_id][-1][3] - pers_frame[j][3])/float(diff_frames_store))),
								float(pers[pers_id][-1][4]) - it*(float((pers[pers_id][-1][4] - pers_frame[j][4])/float(diff_frames_store))),
								float(pers[pers_id][-1][5]) - it*(float((pers[pers_id][-1][5] - pers_frame[j][5])/float(diff_frames_store)))]
				pers[pers_id].append(pers_append)
			duration = abs(pers[pers_id][0][0] - pers_frame[j][0])
			# durcheck = 60*0.25
			durcheck = fps*0.25
			if duration < durcheck:
				dur = final_dict[pers_id][pers[pers_id][0][0]]["Direction"]
			else:
				if (pers_frame[j][5] - pers[pers_id][0][5]) > 20:
					dur = "Ahead"
				elif (pers_frame[j][5] - pers[pers_id][0][5]) < 20:
					dur = "Back"
				else:
					dur = "None"
			final_dict[pers_id][pers_frame[j][0]] = {"cord":{"X":pers_frame[j][2], "Y":pers_frame[j][3]}, "Direction": dur}
		else:
			new_per = len(pers)
			pers[new_per] = [pers_frame[j]]
			final_dict[new_per] = {pers_frame[j][0]:{"cord":{"X":pers_frame[j][2], "Y":pers_frame[j][3]}, "Direction": "None"}}
	return pers, final_dict

def track(csv_file):
	person_data= np.genfromtxt("csvPed_"+csv_file+"_2.csv", delimiter = ',')
	pers ={}
	final_dict = {}
	pers_frame = []
	marker = 0
	last_frame = person_data[-1][0]
	for i in range(1,len(person_data)):
		# To start the tracking from the first frame
		if person_data[1][0] == person_data[i][0]:
			x,y = calcentroid(person_data[i])
			pers_framet  = [person_data[i][0], person_data[i][1], person_data[i][2], person_data[i][3], x,y]
			pers_frame.append(pers_framet)
			marker = i
		else:
			break
	pers_frame = filterpers(pers_frame)
	for i in range(len(pers_frame)):
		pers[i] = [pers_frame[i]]
		final_dict[i] = {pers_frame[i][0]:{"cord":{"X":pers_frame[i][2], "Y":pers_frame[i][3]}, "Direction": "None"}}
	marker = marker + 1
	markkk = 200000000000000
	for num_f in range(2, int(last_frame)):
		pers_frame = []
		while (True):
			fnum = int(person_data[marker][0])
			if ( fnum != num_f):
				break
			x,y = calcentroid(person_data[marker])
			pers_framet = [person_data[marker][0], person_data[marker][1], person_data[marker][2], person_data[marker][3], x,y]
			pers_frame.append(pers_framet)
			marker = marker+1
		pers_frame = filterpers(pers_frame)
		pers, final_dict = samepers(pers_frame,pers,final_dict)
		if markkk < 0:
			with open('ped_track_j_'+csv_file+'.json', 'w') as fp:
				json.dump(final_dict, fp, sort_keys=True, indent=4)
		else:
			markkk = markkk -1
	final_dict_copy = final_dict.copy()
	for s in final_dict:
		if len(final_dict[s]) < min_length:
			del final_dict_copy[s]
	print(len(final_dict_copy))
	with open('ped_track_j_'+csv_file+'.json', 'w') as fp:
		json.dump(final_dict_copy, fp, sort_keys=True, indent=4)

if __name__ == '__main__':
	track("GH010228")
	track("GH020228")
	track("GH030228")
	track("GH040228")
	track("GH050228")
	track("GH060228")
	track("GH070228")
	track("GH080228")
	track("GH090228")
	track("GH100228")