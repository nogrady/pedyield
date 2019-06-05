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
max_time_wait = 2.5
# TTL frames
min_length = 30

# define zone 1
z_one_bottom=350
z_one_left=700
# define zone 2
z_two_bottom=215
z_two_up_right=2700
z_two_down_left=350
# sanity check
sanity_right=1750
sanity_down=225

# line to define the tracking zone
def lines(x1,y1):
	y3 = -(z_two_up_right/z_two_down_left)*x1 + z_two_up_right
	if y1 >= y3:
		return True
	else:
		return False

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

# calculate the area of a rectangle
def area_rec(pers):
	h = abs(pers[2]-pers[6])
	w = abs(pers[3] - pers[7])
	return (h*w)

# overlap percentage
def percentOverlap(persnow, persnew):
	Anow = area_rec(persnow)
	Anew = area_rec(persnew)
	AOver = (max(persnow[3],persnew[3])- min(persnow[7],persnew[7]))*(max(persnow[6],persnew[6]) - min(persnow[2],persnow[2]))
	try: 
		pp = AOver/(Anow+Anew - AOver)
	except:
		pp = 0.0
	PNow = AOver/Anow
	PNew = AOver/Anew
	df = max(PNow, PNew, pp)
	return pp

def oveframe(persnew,persnow):
	if (not (persnow[6] > persnew[2] or persnow[2] < persnew[6] or persnow[7] < persnew[3] or persnow[3] > persnew[7])):
		perover = percentOverlap(persnew, persnow)
		return perover
	else:
		return 0.0

# remove the duplicates
def filterhelp(fil_pers):
	fil_return = []
	fil_return_copy =[]
	to_Del = []
	backup_list = {}
	check = True
	id = -1
	# to verify if the duplicates are the same and delete the duplicates
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

# using overlapped percentage to remove the duplicates
def filterpers(pers_frame):
	fil_pers = []
	unique_pers = []
	for i in range(len(pers_frame)):
		persnow = pers_frame[i]
		for j in range(i+1, len(pers_frame)):
			persnew = pers_frame[j]
			if oveframe(persnew, persnow) > 0.85:
				dd = [i,j]
				fil_pers.append(dd)
	# list of cars/peds to be removed
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
					unique_pers.append(mean_per[0])
					throw = False
					break
			if throw:
				unique_pers.append(pers_frame[i])
	return unique_pers

def samepers(pers_frame, pers, final_dict):
	per_done = []
	tt = {}
	gg = {}
	for j in range(len(pers_frame)):
		min_dist = 1000000000000
		pers_id = -1
		diff_frames_store = -1
		mscp = 0.0
		tt[j] = -1
		for i in range(len(pers)-1):
			if i not in gg:
				gg[i] = 0.0
			if i not in per_done:
				last_frame = pers[i][-1]
				dis = distance(last_frame[2],last_frame[3], pers_frame[j][2],pers_frame[j][3])
				diff_frames = abs(pers_frame[j][0] - last_frame[0])
				perover = oveframe(last_frame, pers_frame[j])
				# trackng the cars and get the right car to track
				if mscp < perover and perover > 0.50 and abs(diff_frames) < fps*max_time_wait:
					if i==1 and last_frame[0] >17:
						print("got it", dis)
					min_dist = dis
					pers_id = i
					diff_frames_store = abs(diff_frames)
					mscp = perover
					gg[i] = perover
					for h in tt:
						if h != j:
							tt[h] = -1
					tt[j] = i
		per_done.append(pers_id)
		if pers_id != -1:
			# calculate and generate the missing frame values
			for it in range(1, int(diff_frames_store)+1):
				pers_append = [pers[pers_id][-1][0] + 1, it*(float(pers_frame[j][1])/diff_frames_store), 
								float(pers[pers_id][-1][2]) - it*(float((pers[pers_id][-1][2] - pers_frame[j][2])/float(diff_frames_store))),
								float(pers[pers_id][-1][3]) - it*(float((pers[pers_id][-1][3] - pers_frame[j][3])/float(diff_frames_store))),
								float(pers[pers_id][-1][4]) - it*(float((pers[pers_id][-1][4] - pers_frame[j][4])/float(diff_frames_store))),
								float(pers[pers_id][-1][5]) - it*(float((pers[pers_id][-1][5] - pers_frame[j][5])/float(diff_frames_store))),
								float(pers[pers_id][-1][6]) - it*(float((pers[pers_id][-1][6] - pers_frame[j][6])/float(diff_frames_store))),
								float(pers[pers_id][-1][7]) - it*(float((pers[pers_id][-1][7] - pers_frame[j][7])/float(diff_frames_store)))]
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
			final_dict[pers_id][pers_frame[j][0]] = {"cord":{"X":pers_frame[j][2], "Y":pers_frame[j][3], "top":pers_frame[j][6], "right":pers_frame[j][7]}, "Direction": dur}
		else:
			if True: 
				# need to pre-define the zone 1
				# track new cars start at zone 1
				if pers_frame[j][3] <z_one_left and pers_frame[j][2] > z_one_bottom:
					new_per = len(pers)
					pers[new_per] = [pers_frame[j]]
					final_dict[new_per] = {pers_frame[j][0]:{"cord":{"X":pers_frame[j][2], "Y":pers_frame[j][3], "top":pers_frame[j][6], "right":pers_frame[j][7]}, "Direction": "None"}}
	return pers, final_dict

def track(csv_file):
	person_data= np.genfromtxt("csvCar_"+csv_file+"_2.csv", delimiter = ',')
	pers ={}
	final_dict = {}
	pers_frame = []
	marker = 0
	last_frame = person_data[-1][0]
	for i in range(1,len(person_data)):
		# To start the tracking from the first frame
		if person_data[1][0] == person_data[i][0]:
			x,y = calcentroid(person_data[i])
			# get all the cars appeared in zone 1 at the first frame
			# z_one_bottom z_one_left are video dependent, defining the area of zone 1
			if person_data[i][2] > z_one_bottom and person_data[i][3] < z_one_left:
				pers_framet  = [person_data[i][0], person_data[i][1], person_data[i][2], person_data[i][3], x,y, person_data[i][4], person_data[i][5]]
				pers_frame.append(pers_framet)
			marker = i
		else:
			break
	# remove the duplicates
	pers_frame = filterpers(pers_frame)
	k = 0
	for i in range(len(pers_frame)):
		if True: 
			pers[k] = [pers_frame[i]]
			final_dict[k] = {pers_frame[i][0]:{"cord":{"X":pers_frame[i][2], "Y":pers_frame[i][3], "top":pers_frame[i][6], "right":pers_frame[i][7]}, "Direction": "None"}}
			k = k + 1
	marker = marker + 1
	markkk = 200000000000000
	for num_f in range(2, int(last_frame)):
		pers_frame = []
		while (True):
			fnum = int(person_data[marker][0])
			if ( fnum != num_f):
				print("Current frame: ", fnum)
				break
			
			x,y = calcentroid(person_data[marker])
			# check if the car is in zone 2 or zone 1
			if ((person_data[marker][2]> z_two_bottom) and lines(person_data[marker][2], person_data[marker][3])) or (person_data[i][2] > z_one_bottom and person_data[i][3] < z_one_left):
				pers_framet = [person_data[marker][0], person_data[marker][1], person_data[marker][2], person_data[marker][3], x,y, person_data[marker][4], person_data[marker][5]]
				pers_frame.append(pers_framet)
			marker = marker+1
		pers_frame = filterpers(pers_frame)
		pers, final_dict = samepers(pers_frame,pers,final_dict)
		if markkk < 0:
			with open('car_track_j_'+csv_file+'.json', 'w') as fp:
				json.dump(final_dict, fp, sort_keys=True, indent=4)
		else:
			markkk = markkk -1
	final_dict_copy = final_dict.copy()
	to_write = [["frame","time", "ID", "bottom", "left", "top","right"]]
	tpwr  = []
	tocross = []
	# remove some cars that have less appearing time
	for s in final_dict:
		if len(final_dict[s]) < min_length:
			del final_dict_copy[s]
		else:
			lastfr = list(final_dict[s].keys())[-1]
			Y = int(final_dict[s][lastfr]["cord"]["Y"])
			final_dict_cp2 = final_dict_copy.copy()
			for fam in list(final_dict_cp2[s].keys()):
				Y = int(final_dict_copy[s][fam]["cord"]["Y"])
				X = int(final_dict_copy[s][fam]["cord"]["X"])
				# remove cars appeared in the top left zone (sanity check)
				if Y < sanity_right and X < sanity_down:
					del final_dict_copy[s][fam]
				else:
					to_ap = [fam, float(float(fam)/fps), s,final_dict[s][fam]["cord"]["X"],final_dict[s][fam]["cord"]["Y"], final_dict[s][fam]["cord"]["top"], final_dict[s][fam]["cord"]["right"] ]
					if Y > 1050 and s not in tocross:
						tocross.append(s)
					tpwr.append(to_ap)
	sort_tpwr = sorted(tpwr, key = lambda x:x[0])
	to_write = to_write + sort_tpwr
	for gh in range(5):
		print(to_write[gh])
	with open("car_track_"+csv_file+".csv", "w+") as csv_write:
		csvwrite = csv.writer(csv_write, delimiter = ',')
		csvwrite.writerows(to_write)
	print(len(final_dict_copy))
	print("cars crossed: ", len(tocross))
	with open('car_track_j_'+csv_file+'.json', 'w') as fp:
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