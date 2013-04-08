/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.jca.core.connectionmanager.pool;

import org.jboss.jca.core.api.connectionmanager.pool.PoolStatistics;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPool;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnection;
import org.jboss.jca.core.connectionmanager.rar.SimpleManagedConnectionFactory1;
import org.jboss.jca.embedded.dsl.ironjacamar11.api.ConnectionDefinitionType;
import org.jboss.jca.embedded.dsl.ironjacamar11.api.IronjacamarDescriptor;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import static org.junit.Assert.*;

/**
 * 
 * A PoolByCriNoTxDeploymentSimpleTestCase
 * 
 * NOTE that this class is in org.jboss.jca.core.connectionmanager.pool and not in
 * org.jboss.jca.core.connectionmanager.pool.strategy because it needs to access to 
 * AbstractPool's package protected methods.
 * Please don't move it, and keep this class packaging consistent with AbstractPool's
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * 
 */
public class PoolByCriNoTxDeploymentSimpleTestCase extends PoolByCriNoTxTestCaseAbstract
{

   /**
    * 
    * deployment
    * 
    * @return archive
    */
   @Deployment
   public static ResourceAdapterArchive deployment()
   {
      return createNoTxDeployment(getIJ());
   }

   /**
    * 
    * get IronjacamarDescriptor for deployment
    * 
    * @return IronjacamarDescriptor
    */
   public static IronjacamarDescriptor getIJ()
   {
      IronjacamarDescriptor ij = getBasicIJXml(SimpleManagedConnectionFactory1.class.getName());
      ConnectionDefinitionType ijCdt = ij.getOrCreateConnectionDefinitions().getOrCreateConnectionDefinition();
      ijCdt.getOrCreateSecurity().application();

      return ij;
   }

   @Override
   public void checkPool() throws Exception
   {
      AbstractPool pool = getPool();

      assertEquals(pool.getManagedConnectionPools().size(), 0);
      SimpleConnection c = cf.getConnection();
      assertEquals(pool.getManagedConnectionPools().size(), 1);
      PoolStatistics ps = pool.getStatistics();
      checkStatistics(ps, 19, 1, 1);

      c.close();
      assertEquals(pool.getManagedConnectionPools().size(), 1);
      checkStatistics(ps, 20, 0, 1);

      c = cf.getConnection("0");
      SimpleConnection c1 = cf.getConnection("1");
      assertEquals(pool.getManagedConnectionPools().size(), 3);
      checkStatistics(ps, 58, 2, 3);

      for (ManagedConnectionPool mcp : pool.getManagedConnectionPools().values())
      {
         if (mcp.getStatistics().getAvailableCount() == 20)
            checkStatistics(mcp.getStatistics(), 20, 0, 1);
         else
            checkStatistics(mcp.getStatistics(), 19, 1, 1);
      }

      c.fail();
      c1.close();
      assertEquals(pool.getManagedConnectionPools().size(), 3);
      checkStatistics(ps, 60, 0, 2, 1);
   }
}