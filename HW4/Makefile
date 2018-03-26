# Package containers inside src directory
PACKAGES = 

# Compiler
JAVAC = javac
JVM = 1.8

# Directory for compiled binaries
BIN = ./build/

# Source file directory
SRC = ./src/
HADOOP = /etc/hadoop:/usr/lib/hadoop-3.0.1/share/hadoop/common/lib/*:/usr/lib/hadoop-3.0.1/share/hadoop/common/*:/usr/lib/hadoop-3.0.1/share/hadoop/hdfs:/usr/lib/hadoop-3.0.1/share/hadoop/hdfs/lib/*:/usr/lib/hadoop-3.0.1/share/hadoop/hdfs/*:/usr/lib/hadoop-3.0.1/share/hadoop/mapreduce/*:/usr/lib/hadoop-3.0.1/share/hadoop/yarn:/usr/lib/hadoop-3.0.1/share/hadoop/yarn/lib/*:/usr/lib/hadoop-3.0.1/share/hadoop/yarn/*

# Compiler flags
JAVAFLAGS = -g -d $(BIN) -cp $(SRC) -cp $(HADOOP) -target $(JVM)

# Create .class file
COMPILE = $(JAVAC) $(JAVAFLAGS)

EMPTY = 

JAVA_FILES = $(subst $(SRC), $(EMPTY), $(wildcard $(SRC)*.java))

ifdef PACKAGES
PACKAGEDIRS = $(addprefix $(SRC), $(PACKAGES))
PACKAGEFILES = $(subst $(SRC), $(EMPTY), $(foreach DIR, $(PACKAGEDIRS), $(wildcard $(DIR)/*.java)))
ALL_FILES = $(PACKAGEFILES) $(JAVA_FILES)
else
#ALL_FILES = $(wildcard $(SRC).java)
ALL_FILES = $(JAVA_FILES)
endif

# "main" class listed in Runfile
# CLASS_FILES = $(subst $(SRC), $(BIN), $(ALL_FILES:.java=.class))
CLASS_FILES = $(ALL_FILES:.java=.class)

all : $(addprefix $(BIN), $(CLASS_FILES))

# Action starts with <TAB>
$(BIN)%.class : $(SRC)%.java
	@ mkdir -p $(BIN)
	$(COMPILE) $<

clean :
	rm -rf $(BIN)*
