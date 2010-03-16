
#include <windows.h>
#include <process.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <unistd.h>
#include <errno.h>
#include <sys/stat.h>

char * getcwd ()
     {
       size_t size = 300;

       while (1)
         {
           char *buffer = (char *) malloc((size + 1 ) * sizeof(char));
           errno = 0;
           if (getcwd (buffer, size) == buffer)
             return buffer;
           free (buffer);
           if (errno != ERANGE)
             return 0;
           size *= 2;
         }
     }

char *concat(const char *argv[])
{
   int concatStrLen = 0;
   for(int i = 0; argv[i] != 0; i++)
   {
      concatStrLen += strlen(argv[i]);
   }
   char *result = (char *) malloc((concatStrLen + 1 ) * sizeof(char));
   int pos = 0;
    for(int i = 0; argv[i] != 0; i++)
   {
      const char* arg = argv[i];
      strcpy(result+pos , arg);
      pos += strlen(arg);
   }
   return result;
}

char *surround_by_quote(const char *in_string) {
   const char *argv[] = {"\"", in_string, "\"", 0};
   return concat(argv);
}

char *param2define(int number, const char *in_string) {
   char buf[10];
   sprintf(buf, "%u", number);
   const char *argv[] = {"\"-Dorg.freeplane.param", buf, "=", in_string, "\"", 0};
   return concat(argv);
}

int main(int argc, char *argv[])  {
    // argv[0] - caller name, argv[argc -1] == last argument,

   int no_of_fixed_arguments = 11;
   int one_for_stopping_null = 1;
   int no_of_passed_arguments_without_caller = argc - 1;

   int no_of_passed_arguments = no_of_fixed_arguments
            + no_of_passed_arguments_without_caller + one_for_stopping_null;

   // Pick the path from argv[0]. This is for the case that the launcher is not
   // started from the folder in which it resides.

   char* path_to_launcher = argv[0];
   char* path_to_launcher_without_file = ".\\";
   if (char *position_of_last_occurrence = strrchr(path_to_launcher,'\\')) {
      int prefix_length = position_of_last_occurrence - path_to_launcher + 1;

      path_to_launcher_without_file = (char *) malloc((prefix_length +
                                             one_for_stopping_null ) * sizeof(char));
      strncpy(path_to_launcher_without_file, path_to_launcher, prefix_length);
      path_to_launcher_without_file[prefix_length] = '\0'; // End the string with null.
   }


#ifdef PORTABLE_APP
      char * pathToPortableApp = path_to_launcher_without_file;
      char*  javaw_path;
      {
         const char *argv[] = {pathToPortableApp, "App\\Freeplane\\", 0};
         pathToPortableApp = path_to_launcher_without_file;
         path_to_launcher_without_file = concat(argv);
      }
      {
         const int pathToPortableAppLen = strlen(pathToPortableApp);
         char* pathToPortableJava = pathToPortableApp;
         int pos;
         for(pos = pathToPortableAppLen - 1; 
            pos > 0 && pathToPortableJava[--pos] != '\\';);
         if(pos == 0)
         {
            pathToPortableJava = "..\\";
         }   
         else
         {
            pos++;
            pathToPortableJava =(char *) malloc((pos + 1 ) * sizeof(char));
            strncpy(pathToPortableJava, path_to_launcher_without_file, pos);
            pathToPortableJava[pos] = 0;
         }
         {
            const char *argv[] = {pathToPortableJava, "CommonFiles\\Java\\bin\\javaw.exe", 0};
            javaw_path = concat(argv);
         }
      }   
      no_of_passed_arguments+=2;
#else 
#ifdef CONSOLE_APP 
      char * javaw_path = "java.exe";
#else  
      char * javaw_path = "javaw.exe";
#endif
#endif

   char** arguments = (char **) malloc(no_of_passed_arguments * sizeof(char*));
   int argumentNumber = 0;
   #ifdef PORTABLE_APP
   arguments[argumentNumber++] = surround_by_quote(javaw_path);
   #else
   arguments[argumentNumber++] = javaw_path;
   #endif
   
   const char* freeplaneMaxHeapSizeEnv = getenv("FREEPLANE_MAX_HEAP_SIZE");
   char* argument_allowing_more_memory;
   if(freeplaneMaxHeapSizeEnv)
   {
      const char *argv[] = {"-Xmx", freeplaneMaxHeapSizeEnv, 0};
      argument_allowing_more_memory = concat(argv);
   }
   else
   {
      argument_allowing_more_memory = "-Xmx512m";
   }
   arguments[argumentNumber++] = argument_allowing_more_memory;

#ifdef PORTABLE_APP
   {
      arguments[argumentNumber++] = "\"-Dportableapp=true\"";
      const char *argv[] = {"\"-Duser.home=", pathToPortableApp, "Data\"", 0};
      arguments[argumentNumber++] = concat(argv);
   }
#endif

   {
      const char *argv[] = {"\"-Dorg.knopflerfish.framework.bundlestorage=memory\"", 0};
      arguments[argumentNumber++] = concat(argv);
   }

   {
      const char *argv[] = {"\"-Dorg.freeplane.globalresourcedir=", path_to_launcher_without_file, "resources\"", 0};
      arguments[argumentNumber++] = concat(argv);
   }

   {
      const char *argv[] = {"\"-Dorg.knopflerfish.gosg.jars=reference:file:", path_to_launcher_without_file, "core/\"", 0};
      arguments[argumentNumber++] = concat(argv);
   }

   for (int i=1; i <= no_of_passed_arguments_without_caller; ++i) {
      arguments[argumentNumber++] = param2define(i, argv[i]);

   }

   arguments[argumentNumber++] = "-jar";

   {
      const char *argv[] = {path_to_launcher_without_file, "framework.jar", 0};
      arguments[argumentNumber++] = surround_by_quote(concat(argv));
   }

   arguments[argumentNumber++] = "-xargs";
   {
      const char *argv[] = {path_to_launcher_without_file, "props.xargs", 0};
      arguments[argumentNumber++] = surround_by_quote(concat(argv));
   }
   arguments[argumentNumber++] = "-xargs";
   {
      const char *argv[] = {path_to_launcher_without_file, "init.xargs", 0};
      arguments[argumentNumber++] = surround_by_quote(concat(argv));
   }
   // Null-terminate the arguments array

   arguments[argumentNumber++] = (char *)0;

#if defined (__DEBUG__) || defined (CONSOLE_APP)
      for (int i=0; i < argumentNumber; ++i) {
         printf("Argument %s\n",arguments[i]);
      }
#endif
   // Replace current process by a new one running our application

#ifdef PORTABLE_APP
   _execv(javaw_path, arguments);
   arguments[0] = "javaw.exe";
#endif
   _execvp(arguments[0], arguments);
   // the following patch seems useful for vista but needs additional testing.
   // https://sourceforge.net/tracker/?func=detail&atid=107118&aid=2350483&group_id=7118
   // Submitted By: Mario Valle (mvalle58)
   // Summary: Windows launcher does nothing (+ solution)
   // _spawnvp(_P_DETACH, arguments[0], arguments);

   return 0;
}
