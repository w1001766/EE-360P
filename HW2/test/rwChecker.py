file_name = "tmp.log"

with open(file_name, 'r') as f:
  s = f.readlines()
  begin_read = 0
  writing = 0
  reading = 0
  end_write = 0
  begin_write = 0
  end_read = 0

  for line in s:
    if 'Attempting Reading:' in line:
      begin_read += 1
    elif 'Reading: ' in line:
      reading += 1
      begin_read -= 1
      if writing != 0:
        print ('Reader+Writer in CS')
    elif 'End Read: ' in line:
      reading -= 1
      end_read += 1
    elif 'Attempting Write: ' in line:
      begin_write += 1
    elif 'Writing: ' in line:
      begin_write -= 1
      writing += 1
      if reading != 0:
        print ('Reader+Writer in CS')
      if writing != 1:
        print ('Multiple writers in CS')
    elif 'End Write: ' in line:
      writing -= 1
      end_write += 1
    else:
      pass
    if 'exception' in line.lower():
      print (line)

    if end_write == 0 or end_read == 0:
      print ("No writers/readers")
