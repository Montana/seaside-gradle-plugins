//------------------------------------------------------------------------------
//  UNCLASSIFIED (U)
//------------------------------------------------------------------------------

#include <algorithm>

#include "logger/LogLevel.h"
//#include "gem/utility/String.h"


namespace blocs { namespace basiclogservice {

      std::mutex  LogLevel::mapInitializationGuard;
      bool LogLevel::mapInitialized = false;
      LogLevel::EnumerationMap LogLevel::stringValues = LogLevel::EnumerationMap();

      void LogLevel::populateMap(void) {
         if (mapInitialized) return;
         std::lock_guard<std::mutex> lock(LogLevel::mapInitializationGuard);
         if (mapInitialized) return; //check again now that we have the lock in case we were blocked while somebody else initialized

         stringValues[ALL]           = "ALL";
         stringValues[DEBUG_FINE]    = "FINE";
         stringValues[DEBUG_REGULAR] = "DEBUG";
         stringValues[INFO]          = "INFO";
         stringValues[WARN]          = "WARN";
         stringValues[USERGRAM]      = "USERGRAM";
         stringValues[LOG_ERROR]     = "ERROR";
         stringValues[FATAL]         = "FATAL";
         stringValues[NONE]          = "NONE";

         mapInitialized = true;
      }

      LogLevel::Values
      LogLevel::convertToEnum(const std::string& value)
      throw (std::range_error) {

         populateMap();

         std::string upperCaseValue = value;
         //String::transformStringToUpper(upperCaseValue);
         std::transform(upperCaseValue.begin(), upperCaseValue.end(), upperCaseValue.begin(), ::toupper);

         LogLevel::EnumerationMap::const_iterator finder;

         LogLevel::EnumerationMap::const_iterator end = stringValues.end();

         for (finder = stringValues.begin(); finder != end; ++finder) {
            if (finder->second == upperCaseValue) {
               return finder->first;
            }
         }

         throw std::range_error("LogLevel::convertToEnum()");
      }

      const std::string&
      LogLevel::convertToString(LogLevel::Values value)
      throw (std::range_error) {
         populateMap();

         EnumerationMap::const_iterator finder = stringValues.find(value);

         if (finder != stringValues.end()) {
            return finder->second;
         }

         throw std::range_error("LogLevel::convertToString()");
      }

      const LogLevel::EnumerationMap& LogLevel::getEnumerationMap(void) {
         populateMap();
         return stringValues;
      }

}}

