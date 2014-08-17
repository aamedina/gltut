# GNU Make project makefile autogenerated by Premake
ifndef config
  config=debug
endif

ifndef verbose
  SILENT = @
endif

ifndef CC
  CC = gcc
endif

ifndef CXX
  CXX = g++
endif

ifndef AR
  AR = ar
endif

ifeq ($(config),debug)
  OBJDIR     = obj/Debug/Tut\ 05\ Base\ Vertex\ with\ Overlap
  TARGETDIR  = .
  TARGET     = $(TARGETDIR)/Tut\ 05\ Base\ Vertex\ with\ OverlapD
  DEFINES   += -D_CRT_SECURE_NO_WARNINGS -D_CRT_SECURE_NO_DEPRECATE -D_SCL_SECURE_NO_WARNINGS -DTIXML_USE_STL -DFREEGLUT_STATIC -D_LIB -DFREEGLUT_LIB_PRAGMAS=0 -DDEBUG -D_DEBUG
  INCLUDES  += -I../framework -I../glsdk/glload/include -I../glsdk/glimg/include -I../glsdk/glm -I../glsdk/glutil/include -I../glsdk/glmesh/include -I../glsdk/freeglut/include
  CPPFLAGS  += -MMD -MP $(DEFINES) $(INCLUDES)
  CFLAGS    += $(CPPFLAGS) $(ARCH) -g
  CXXFLAGS  += $(CFLAGS) 
  LDFLAGS   += -L../glsdk/glload/lib -L../glsdk/glimg/lib -L../glsdk/glutil/lib -L../glsdk/glmesh/lib -L../glsdk/freeglut/lib -L../framework/lib
  LIBS      += -lframeworkD -lglloadD -lglimgD -lglutilD -lglmeshD -lfreeglutD
  RESFLAGS  += $(DEFINES) $(INCLUDES) 
  LDDEPS    += ../framework/lib/libframeworkD.a
  LINKCMD    = $(CXX) -o $(TARGET) $(OBJECTS) $(LDFLAGS) $(RESOURCES) $(ARCH) $(LIBS)
  define PREBUILDCMDS
  endef
  define PRELINKCMDS
  endef
  define POSTBUILDCMDS
  endef
endif

ifeq ($(config),release)
  OBJDIR     = obj/Release/Tut\ 05\ Base\ Vertex\ with\ Overlap
  TARGETDIR  = .
  TARGET     = $(TARGETDIR)/Tut\ 05\ Base\ Vertex\ with\ Overlap
  DEFINES   += -D_CRT_SECURE_NO_WARNINGS -D_CRT_SECURE_NO_DEPRECATE -D_SCL_SECURE_NO_WARNINGS -DTIXML_USE_STL -DFREEGLUT_STATIC -D_LIB -DFREEGLUT_LIB_PRAGMAS=0 -DRELEASE -DNDEBUG
  INCLUDES  += -I../framework -I../glsdk/glload/include -I../glsdk/glimg/include -I../glsdk/glm -I../glsdk/glutil/include -I../glsdk/glmesh/include -I../glsdk/freeglut/include
  CPPFLAGS  += -MMD -MP $(DEFINES) $(INCLUDES)
  CFLAGS    += $(CPPFLAGS) $(ARCH) -O3 -fomit-frame-pointer -Wall
  CXXFLAGS  += $(CFLAGS) 
  LDFLAGS   += -Wl,-x -L../glsdk/glload/lib -L../glsdk/glimg/lib -L../glsdk/glutil/lib -L../glsdk/glmesh/lib -L../glsdk/freeglut/lib -L../framework/lib
  LIBS      += -lframework -lglload -lglimg -lglutil -lglmesh -lfreeglut
  RESFLAGS  += $(DEFINES) $(INCLUDES) 
  LDDEPS    += ../framework/lib/libframework.a
  LINKCMD    = $(CXX) -o $(TARGET) $(OBJECTS) $(LDFLAGS) $(RESOURCES) $(ARCH) $(LIBS)
  define PREBUILDCMDS
  endef
  define PRELINKCMDS
  endef
  define POSTBUILDCMDS
  endef
endif

OBJECTS := \
	$(OBJDIR)/BaseVertexOverlap.o \

RESOURCES := \

SHELLTYPE := msdos
ifeq (,$(ComSpec)$(COMSPEC))
  SHELLTYPE := posix
endif
ifeq (/bin,$(findstring /bin,$(SHELL)))
  SHELLTYPE := posix
endif

.PHONY: clean prebuild prelink

all: $(TARGETDIR) $(OBJDIR) prebuild prelink $(TARGET)
	@:

$(TARGET): $(GCH) $(OBJECTS) $(LDDEPS) $(RESOURCES)
	@echo Linking Tut 05 Base Vertex with Overlap
	$(SILENT) $(LINKCMD)
	$(POSTBUILDCMDS)

$(TARGETDIR):
	@echo Creating $(TARGETDIR)
ifeq (posix,$(SHELLTYPE))
	$(SILENT) mkdir -p $(TARGETDIR)
else
	$(SILENT) mkdir $(subst /,\\,$(TARGETDIR))
endif

$(OBJDIR):
	@echo Creating $(OBJDIR)
ifeq (posix,$(SHELLTYPE))
	$(SILENT) mkdir -p $(OBJDIR)
else
	$(SILENT) mkdir $(subst /,\\,$(OBJDIR))
endif

clean:
	@echo Cleaning Tut 05 Base Vertex with Overlap
ifeq (posix,$(SHELLTYPE))
	$(SILENT) rm -f  $(TARGET)
	$(SILENT) rm -rf $(OBJDIR)
else
	$(SILENT) if exist $(subst /,\\,$(TARGET)) del $(subst /,\\,$(TARGET))
	$(SILENT) if exist $(subst /,\\,$(OBJDIR)) rmdir /s /q $(subst /,\\,$(OBJDIR))
endif

prebuild:
	$(PREBUILDCMDS)

prelink:
	$(PRELINKCMDS)

ifneq (,$(PCH))
$(GCH): $(PCH)
	@echo $(notdir $<)
	-$(SILENT) cp $< $(OBJDIR)
	$(SILENT) $(CXX) $(CXXFLAGS) -o "$@" -c "$<"
endif

$(OBJDIR)/BaseVertexOverlap.o: BaseVertexOverlap.cpp
	@echo $(notdir $<)
	$(SILENT) $(CXX) $(CXXFLAGS) -o "$@" -c "$<"

-include $(OBJECTS:%.o=%.d)
