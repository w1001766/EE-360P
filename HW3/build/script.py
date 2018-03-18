import os
import time
from multiprocessing import Process

from colorama import init
init()

from colorama import Fore, Back, Style


def print_red(s):
	print(Fore.RED + str(s))
	print(Style.RESET_ALL)

def print_green(s):
	print(Fore.GREEN + str(s))
	print(Style.RESET_ALL)


client_command = "java BookClient "
client_path = "cmdFile"


server_command = "java BookServer "
#server_path = "/Users/KaTaiHo/Documents/workspace/360P_Lab3/bin/"
server_file = "input.txt"

def clean_up():
	for i in range(1, 11, 1):
		os.system("rm out_" + str(i) + ".txt")

def testcase1():
	def start_server():
		os.system(server_command + server_path +server_file)

	# start_server()

	k = 0
	def spawn_client():
		os.system(client_command + client_path + str(k%2)  + " " + str(k))
		# print ("process " + str(k) + " has started")


	for i in range(0, 10, 1):
		k += 1
		p = Process(target = spawn_client)
		p.start()

	time.sleep(4)

	count = 0 

	for i in range(1, 11, 1):
		outputFile = "out_" + str(i) + ".txt"
		# print "parsed " + outputFile

		with open(outputFile) as f:
			for line in f:	
				if "Mike \"The Letter\"" in line:
					count += 1

	if count != 72:
		print_red("testcase1: failed")
	else:
		print_green("testcase1: passed")
	time.sleep(8)
	clean_up()

def testcase2():
	k = 0
	def spawn_client():
		os.system(client_command + client_path + str(3)  + " " + str(k))
		# print ("process " + str(k) + " has started")


	for i in range(0, 10, 1):
		k += 1
		p = Process(target = spawn_client)
		p.start()

	time.sleep(4)

	count = 0 

	for i in range(1, 11, 1):
		outputFile = "out_" + str(i) + ".txt"
		# print "parsed " + outputFile

		with open("out_test2.txt") as g, open(outputFile) as f:
				for line1, line2 in zip(f,g):	
					if line1.strip() != line2.strip():
						print_red("testcase2: failed")
						print line1 + "from " + outputFile
						print line2 
						return

	print_green("testcase2: passed")

	time.sleep(8)
	clean_up()


def testcase3():
	k = 0
	def spawn_client():
		os.system(client_command + client_path + str(4)  + " " + str(k))
		# print ("process " + str(k) + " has started")


	for i in range(0, 10, 1):
		k += 1
		p = Process(target = spawn_client)
		p.start()

	time.sleep(4)

	count = 0 

	for i in range(1, 11, 1):
		outputFile = "out_" + str(i) + ".txt"
		# print "parsed " + outputFile

		with open("out_test3.txt") as g, open(outputFile) as f:
				for line1, line2 in zip(f,g):	
					if line1.strip() != line2.strip():
						print_red("testcase3: failed")
						print line1 + "from " + outputFile
						print line2 
						return

	print_green("testcase3: passed")

	time.sleep(8)
	clean_up()

def testcase4():
	k = 0
	def spawn_client():
		os.system(client_command + client_path + str(5)  + " " + str(k))
		# print ("process " + str(k) + " has started")


	for i in range(0, 10, 1):
		k += 1
		p = Process(target = spawn_client)
		p.start()

	time.sleep(4)

	# count = 0 

	# for i in range(1, 11, 1):
	# 	outputFile = "out_" + str(i) + ".txt"
	# 	# print "parsed " + outputFile

	# 	with open("out_test3.txt") as g, open(outputFile) as f:
	# 			for line1, line2 in zip(f,g):	
	# 				if line1.strip() != line2.strip():
	# 					print_red("testcase3: failed")
	# 					print line1 + "from " + outputFile
	# 					print line2 
	# 					return

	print_green("testcase4: passed")

	# time.sleep(8)
	# clean_up()

clean_up()
#testcase1()
#testcase2()
testcase3()
#testcase4()