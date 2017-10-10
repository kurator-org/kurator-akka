CFLAGS=-I/usr/share/R/include -I/usr/include/python2.7 -I/usr/include/x86_64-linux-gnu/python2.7 -fno-strict-aliasing -Wdate-time -D_FORTIFY_SOURCE=2 -g -fstack-protector-strong -Wformat -Werror=format-security  -DNDEBUG -g -fwrapv -O2 -Wall -Wstrict-prototypes
LDFLAGS=-L/usr/lib/R/lib -L/usr/lib/python2.7/config-x86_64-linux-gnu -L/usr/lib -lR -lpython2.7 -lpthread -ldl  -lutil -lm  -Xlinker -export-dynamic -Wl,-O1 -Wl,-Bsymbolic-functions

JNIINC=-I/usr/lib/jvm/java-8-oracle/include/ -I/usr/lib/jvm/java-8-oracle/include/linux

VPATH=src/main/c
BUILDDIR=build

SOURCES = $(wildcard $(VPATH)/*.c)
OBJECTS = $(patsubst $(VPATH)/%.c, $(BUILDDIR)/%.o, $(SOURCES))

OPTS=-shared -fPIC
PROGRAMS=libkurator


all: $(PROGRAMS)

$(BUILDDIR)/%.o: %.c
	gcc -c $(JNIINC) $(CFLAGS) $(OPTS) $< -o $@
libkurator: $(OBJECTS)
	gcc $^ $(LDFLAGS) $(OPTS) -o lib/native/libkurator.so
clean:
	rm -f $(PROGRAMS) $(BUILDDIR)/*.o lib/native/libkurator.so
