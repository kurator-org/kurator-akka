VPATH=src/main/c
BUILDDIR=build
LIBDIR=src/main/resources/native

OPTS=-shared -fPIC
PROGRAMS=libkurator

OS := $(shell uname)

ifeq ($(OS),Darwin)

CFLAGS=-I/Library/Frameworks/R.framework/Resources/include/ -I/Library/Frameworks/Python.framework/Versions/2.7/include/python2.7 -I/Library/Frameworks/Python.framework/Versions/2.7/include/python2.7 -fno-strict-aliasing -fno-common -dynamic -arch i386 -arch x86_64 -g -DNDEBUG -g -fwrapv -O3 -Wall -Wstrict-prototypes
LDFLAGS=-L/Library/Frameworks/R.framework/Resources/lib/ -L/Library/Frameworks/Python.framework/Versions/2.7/lib/python2.7/config -lR -lpython2.7 -ldl -framework CoreFoundation

JNIINC=-I/System/Library/Frameworks/JavaVM.framework/Headers

SOURCES = $(wildcard $(VPATH)/*.c)
OBJECTS = $(patsubst $(VPATH)/%.c, $(BUILDDIR)/%.o, $(SOURCES))

all: $(PROGRAMS)

$(BUILDDIR)/%.o: %.c
	gcc -c $(JNIINC) $(CFLAGS) $(OPTS) $< -o $@
libkurator: $(OBJECTS)
	gcc $^ $(LDFLAGS) $(OPTS) -o $(LIBDIR)/libkurator.dylib
clean:
	rm -f $(PROGRAMS) $(BUILDDIR)/*.lipo $(LIBDIR)/libkurator.dylib

else

CFLAGS=-I/usr/share/R/include -I/usr/include/python2.7 -I/usr/include/config-i386-linux-gnu/python2.7 -m32 -fno-strict-aliasing -Wdate-time -D_FORTIFY_SOURCE=2 -g -fstack-protector-strong -Wformat -Werror=format-security  -DNDEBUG -g -fwrapv -O2 -Wall -Wstrict-prototypes
LDFLAGS=-L/usr/lib/R/lib -L/usr/lib/python2.7/config-i386-linux-gnu -L/usr/lib -lR -lpython2.7 -lpthread -ldl  -lutil -lm  -m32 -Xlinker -export-dynamic -Wl,-O1 -Wl,-Bsymbolic-functions

JNIINC=-I/usr/lib/jvm/jdk1.8.0_161/include/ -I/usr/lib/jvm/jdk1.8.0_161/include/linux

SOURCES = $(wildcard $(VPATH)/*.c)
OBJECTS = $(patsubst $(VPATH)/%.c, $(BUILDDIR)/%.o, $(SOURCES))

all: $(PROGRAMS)

$(BUILDDIR)/%.o: %.c
	gcc -c $(JNIINC) $(CFLAGS) $(OPTS) $< -o $@
libkurator: $(OBJECTS)
	gcc $^ $(LDFLAGS) $(OPTS) -o $(LIBDIR)/libkurator.so
clean:
	rm -f $(PROGRAMS) $(BUILDDIR)/*.o $(LIBDIR)/libkurator.so

endif
