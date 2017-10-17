/**
 *
 *  Northrop Grumman Proprietary
 *  ____________________________
 *
 *   Copyright (C) 2017, Northrop Grumman Systems Corporation
 *   All Rights Reserved.
 *
 *  NOTICE:  All information contained herein is, and remains the property of
 *  Northrop Grumman Systems Corporation. The intellectual and technical concepts
 *  contained herein are proprietary to Northrop Grumman Systems Corporation and
 *  may be covered by U.S. and Foreign Patents or patents in process, and are
 *  protected by trade secret or copyright law. Dissemination of this information
 *  or reproduction of this material is strictly forbidden unless prior written
 *  permission is obtained from Northrop Grumman.
 */
#ifndef _BLOCS_IThreadable_H
#define _BLOCS_IThreadable_H


namespace blocs {

      class Threader;

      /**
      IThreadable class represents the active object in the thread.
      */

      class IThreadable {

         public :
    	    virtual ~IThreadable() = default;

            virtual void execute(Threader *threader) = 0;

      };

} //NAMESPACE


#endif

